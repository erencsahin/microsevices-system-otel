package com.microservices.product.service;

import com.microservices.product.model.Product;
import com.microservices.product.repository.ProductRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    private final Tracer tracer;
    
    @Transactional
    public Product createProduct(Product product) {
        Span span = tracer.spanBuilder("createProduct").startSpan();
        try {
            log.info("Creating new product: {}", product.getName());
            
            Product savedProduct = productRepository.save(product);
            span.setAttribute("product.id", savedProduct.getId());
            span.setAttribute("product.name", savedProduct.getName());
            span.setAttribute("product.price", savedProduct.getPrice().doubleValue());
            
            log.info("Product created successfully with ID: {}", savedProduct.getId());
            return savedProduct;
        } catch (Exception e) {
            span.recordException(e);
            log.error("Error creating product", e);
            throw e;
        } finally {
            span.end();
        }
    }
    
    public Product getProductById(Long id) {
        Span span = tracer.spanBuilder("getProductById").startSpan();
        try {
            span.setAttribute("product.id", id);
            log.info("Fetching product with ID: {}", id);
            
            return productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        } catch (Exception e) {
            span.recordException(e);
            log.error("Error fetching product", e);
            throw e;
        } finally {
            span.end();
        }
    }
    
    public List<Product> getAllProducts() {
        Span span = tracer.spanBuilder("getAllProducts").startSpan();
        try {
            log.info("Fetching all products");
            List<Product> products = productRepository.findAll();
            span.setAttribute("products.count", products.size());
            return products;
        } catch (Exception e) {
            span.recordException(e);
            log.error("Error fetching all products", e);
            throw e;
        } finally {
            span.end();
        }
    }
    
    public List<Product> getProductsByCategory(String category) {
        Span span = tracer.spanBuilder("getProductsByCategory").startSpan();
        try {
            span.setAttribute("product.category", category);
            log.info("Fetching products by category: {}", category);
            
            List<Product> products = productRepository.findByCategory(category);
            span.setAttribute("products.count", products.size());
            return products;
        } catch (Exception e) {
            span.recordException(e);
            log.error("Error fetching products by category", e);
            throw e;
        } finally {
            span.end();
        }
    }
    
    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Span span = tracer.spanBuilder("updateProduct").startSpan();
        try {
            span.setAttribute("product.id", id);
            log.info("Updating product with ID: {}", id);
            
            Product product = getProductById(id);
            product.setName(productDetails.getName());
            product.setDescription(productDetails.getDescription());
            product.setPrice(productDetails.getPrice());
            product.setStockQuantity(productDetails.getStockQuantity());
            product.setCategory(productDetails.getCategory());
            
            Product updatedProduct = productRepository.save(product);
            log.info("Product updated successfully with ID: {}", id);
            return updatedProduct;
        } catch (Exception e) {
            span.recordException(e);
            log.error("Error updating product", e);
            throw e;
        } finally {
            span.end();
        }
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        Span span = tracer.spanBuilder("deleteProduct").startSpan();
        try {
            span.setAttribute("product.id", id);
            log.info("Deleting product with ID: {}", id);
            
            Product product = getProductById(id);
            productRepository.delete(product);
            
            log.info("Product deleted successfully with ID: {}", id);
        } catch (Exception e) {
            span.recordException(e);
            log.error("Error deleting product", e);
            throw e;
        } finally {
            span.end();
        }
    }
    
    @Transactional
    public boolean updateStock(Long productId, Integer quantity) {
        Span span = tracer.spanBuilder("updateStock").startSpan();
        try {
            span.setAttribute("product.id", productId);
            span.setAttribute("quantity", quantity);
            log.info("Updating stock for product ID: {} with quantity: {}", productId, quantity);
            
            Product product = getProductById(productId);
            
            if (product.getStockQuantity() < quantity) {
                log.warn("Insufficient stock for product ID: {}", productId);
                span.setAttribute("stock.sufficient", false);
                return false;
            }
            
            product.setStockQuantity(product.getStockQuantity() - quantity);
            productRepository.save(product);
            
            span.setAttribute("stock.sufficient", true);
            log.info("Stock updated successfully for product ID: {}", productId);
            return true;
        } catch (Exception e) {
            span.recordException(e);
            log.error("Error updating stock", e);
            throw e;
        } finally {
            span.end();
        }
    }
}
