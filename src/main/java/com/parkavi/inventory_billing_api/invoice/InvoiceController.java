package com.parkavi.inventory_billing_api.invoice;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody InvoiceRequest request) {
        InvoiceResponse created = invoiceService.createDraft(request);
        return ResponseEntity.created(URI.create("/api/invoices/" + created.id())).body(created);
    }

    @GetMapping
    public List<InvoiceResponse> list() {
        return invoiceService.list();
    }

    @GetMapping("/{id}")
    public InvoiceResponse get(@PathVariable Long id) {
        return invoiceService.get(id);
    }

    @PostMapping("/{id}/confirm")
    public InvoiceResponse confirm(@PathVariable Long id) {
        return invoiceService.confirm(id);
    }

    @PostMapping("/{id}/cancel")
    public InvoiceResponse cancel(@PathVariable Long id) {
        return invoiceService.cancel(id);
    }
}