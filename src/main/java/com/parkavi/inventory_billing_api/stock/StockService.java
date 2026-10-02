package com.parkavi.inventory_billing_api.stock;

import com.parkavi.inventory_billing_api.product.Product;
import com.parkavi.inventory_billing_api.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockMovementRepository movementRepository;
    private final ProductService productService;

    @Transactional
    public StockMovementResponse record(Long productId, StockMovementRequest request) {
        Product product = productService.findOrThrow(productId);
        if (!product.isActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Product is inactive");
        }
        validateDirection(request.reason(), request.quantityChange());

        long current = movementRepository.currentStock(productId);
        if (current + request.quantityChange() < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Insufficient stock: current " + current
                            + ", requested change " + request.quantityChange());
        }

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setQuantityChange(request.quantityChange());
        movement.setReason(request.reason());
        movement.setReference(request.reference());
        return StockMovementResponse.from(movementRepository.save(movement));
    }

    @Transactional(readOnly = true)
    public StockLevelResponse getLevel(Long productId) {
        Product product = productService.findOrThrow(productId);
        long current = movementRepository.currentStock(productId);
        return new StockLevelResponse(
                product.getId(),
                product.getSku(),
                current,
                product.getReorderLevel(),
                current <= product.getReorderLevel());
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> history(Long productId) {
        productService.findOrThrow(productId);
        return movementRepository.findByProductIdOrderByIdDesc(productId).stream()
                .map(StockMovementResponse::from)
                .toList();
    }

    private void validateDirection(StockReason reason, int change) {
        if (change == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantityChange must not be 0");
        }
        switch (reason) {
            case PURCHASE, RETURN -> {
                if (change < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            reason + " must have a positive quantityChange");
                }
            }
            case SALE -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "SALE movements are created automatically by invoices");
            case ADJUSTMENT -> {
                // positive or negative is allowed
            }
        }
    }
}