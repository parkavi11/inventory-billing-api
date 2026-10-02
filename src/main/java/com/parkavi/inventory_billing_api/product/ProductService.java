package com.parkavi.inventory_billing_api.product;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse create(ProductRequest request) {
        String sku = request.sku().trim();
        if (productRepository.existsBySku(sku)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU already exists: " + sku);
        }
        Product product = new Product();
        apply(product, request);
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> list(boolean includeInactive) {
        List<Product> products = includeInactive
                ? productRepository.findAllByOrderByNameAsc()
                : productRepository.findByActiveTrueOrderByNameAsc();
        return products.stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductResponse.from(findOrThrow(id));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findOrThrow(id);
        String sku = request.sku().trim();
        if (!product.getSku().equals(sku) && productRepository.existsBySku(sku)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU already exists: " + sku);
        }
        apply(product, request);
        return ProductResponse.from(product);
    }

    @Transactional
    public void deactivate(Long id) {
        findOrThrow(id).setActive(false);
    }

    public Product findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found: " + id));
    }

    private void apply(Product product, ProductRequest request) {
        product.setSku(request.sku().trim());
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setUnitPrice(request.unitPrice());
        if (request.taxRate() != null) {
            product.setTaxRate(request.taxRate());
        }
        if (request.reorderLevel() != null) {
            product.setReorderLevel(request.reorderLevel());
        }
    }
}