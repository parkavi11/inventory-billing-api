package com.parkavi.inventory_billing_api.stock;

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
@RequestMapping("/api/products/{productId}")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/stock-movements")
    public ResponseEntity<StockMovementResponse> record(
            @PathVariable Long productId,
            @Valid @RequestBody StockMovementRequest request) {
        StockMovementResponse created = stockService.record(productId, request);
        return ResponseEntity
                .created(URI.create("/api/products/" + productId + "/stock-movements/" + created.id()))
                .body(created);
    }

    @GetMapping("/stock-movements")
    public List<StockMovementResponse> history(@PathVariable Long productId) {
        return stockService.history(productId);
    }

    @GetMapping("/stock")
    public StockLevelResponse level(@PathVariable Long productId) {
        return stockService.getLevel(productId);
    }
}