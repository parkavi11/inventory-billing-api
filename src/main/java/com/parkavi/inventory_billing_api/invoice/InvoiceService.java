package com.parkavi.inventory_billing_api.invoice;

import com.parkavi.inventory_billing_api.customer.CustomerService;
import com.parkavi.inventory_billing_api.product.Product;
import com.parkavi.inventory_billing_api.product.ProductRepository;
import com.parkavi.inventory_billing_api.stock.StockMovement;
import com.parkavi.inventory_billing_api.stock.StockMovementRepository;
import com.parkavi.inventory_billing_api.stock.StockReason;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository movementRepository;
    private final CustomerService customerService;

    @Transactional
    public InvoiceResponse createDraft(InvoiceRequest request) {
        // Merge duplicate lines so each product appears once
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (InvoiceItemRequest item : request.items()) {
            quantities.merge(item.productId(), item.quantity(), Integer::sum);
        }

        Map<Long, Product> products = productRepository.findAllById(quantities.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        Invoice invoice = new Invoice();
        if (request.customerId() != null) {
            invoice.setCustomer(customerService.findOrThrow(request.customerId()));
        }
        invoice.setInvoiceNumber(nextInvoiceNumber());

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Product product = products.get(entry.getKey());
            if (product == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found: " + entry.getKey());
            }
            if (!product.isActive()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Product is inactive: " + product.getSku());
            }
            InvoiceItem item = buildItem(product, entry.getValue());
            invoice.addItem(item);
            subtotal = subtotal.add(item.getLineTotal());
            taxTotal = taxTotal.add(item.getTaxAmount());
        }

        invoice.setSubtotal(subtotal);
        invoice.setTaxTotal(taxTotal);
        invoice.setTotal(subtotal.add(taxTotal));
        return InvoiceResponse.from(invoiceRepository.save(invoice));
    }

    @Transactional
    public InvoiceResponse confirm(Long invoiceId) {
        // 1. Lock the invoice so it can't be confirmed twice at once
        Invoice invoice = invoiceRepository.findByIdForUpdate(invoiceId)
                .orElseThrow(() -> notFound(invoiceId));
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only DRAFT invoices can be confirmed (status is " + invoice.getStatus() + ")");
        }

        // 2. Lock the products, always in id order
        List<Long> productIds = invoice.getItems().stream()
                .map(item -> item.getProduct().getId())
                .sorted()
                .toList();
        productRepository.findAllByIdForUpdate(productIds);

        // 3. Check every line while holding the locks
        List<String> shortages = new ArrayList<>();
        for (InvoiceItem item : invoice.getItems()) {
            long available = movementRepository.currentStock(item.getProduct().getId());
            if (available < item.getQuantity()) {
                shortages.add(item.getProduct().getSku()
                        + " (available " + available + ", requested " + item.getQuantity() + ")");
            }
        }
        if (!shortages.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Insufficient stock for: " + String.join("; ", shortages));
        }

        // 4. Write the SALE movements and confirm
        for (InvoiceItem item : invoice.getItems()) {
            StockMovement movement = new StockMovement();
            movement.setProduct(item.getProduct());
            movement.setQuantityChange(-item.getQuantity());
            movement.setReason(StockReason.SALE);
            movement.setReference(invoice.getInvoiceNumber());
            movementRepository.save(movement);
        }
        invoice.setStatus(InvoiceStatus.CONFIRMED);
        invoice.setConfirmedAt(OffsetDateTime.now());
        return InvoiceResponse.from(invoice);
    }

    @Transactional
    public InvoiceResponse cancel(Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdForUpdate(invoiceId)
                .orElseThrow(() -> notFound(invoiceId));
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invoice is already cancelled");
        }
        if (invoice.getStatus() == InvoiceStatus.CONFIRMED) {
            // Put the stock back with compensating RETURN movements
            for (InvoiceItem item : invoice.getItems()) {
                StockMovement movement = new StockMovement();
                movement.setProduct(item.getProduct());
                movement.setQuantityChange(item.getQuantity());
                movement.setReason(StockReason.RETURN);
                movement.setReference("CANCEL " + invoice.getInvoiceNumber());
                movementRepository.save(movement);
            }
        }
        invoice.setStatus(InvoiceStatus.CANCELLED);
        return InvoiceResponse.from(invoice);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse get(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> notFound(invoiceId));
        return InvoiceResponse.from(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> list() {
        return invoiceRepository.findAllByOrderByIdDesc().stream()
                .map(InvoiceResponse::from)
                .toList();
    }

    private InvoiceItem buildItem(Product product, int quantity) {
        BigDecimal lineTotal = product.getUnitPrice()
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = lineTotal
                .multiply(product.getTaxRate())
                .divide(HUNDRED, 2, RoundingMode.HALF_UP);

        InvoiceItem item = new InvoiceItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(product.getUnitPrice());
        item.setTaxRate(product.getTaxRate());
        item.setTaxAmount(taxAmount);
        item.setLineTotal(lineTotal);
        return item;
    }

    private String nextInvoiceNumber() {
        return "INV-" + Year.now().getValue() + "-"
                + String.format("%06d", invoiceRepository.nextInvoiceSequence());
    }

    private ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found: " + id);
    }
}