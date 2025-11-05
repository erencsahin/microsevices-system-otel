package com.microservices.product.service;

import com.microservices.product.model.Product;
import com.microservices.product.repository.ProductRepository;
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

    @Transactional
    public Product createProduct(Product product) {
        try {
            log.info("Creating new product: {}", product.getName());
            
            Product savedProduct = productRepository.save(product);

            log.info("Product created successfully with ID: {}", savedProduct.getId());
            return savedProduct;
        } catch (Exception e) {
            log.error("Error creating product", e);
            throw e;
        } finally {
        }
    }
    
    public Product getProductById(Long id) {
        try {
            log.info("Fetching product with ID: {}", id);
            
            return productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + id));
        } catch (Exception e) {
            log.error("Error fetching product", e);
            throw e;
        }
    }
    
    public List<Product> getAllProducts() {
        try {
            log.info("Fetching all products");
            List<Product> products = productRepository.findAll();
            return products;
        } catch (Exception e) {
            log.error("Error fetching all products", e);
            throw e;
        }
    }
    
    public List<Product> getProductsByCategory(String category) {
        try {
            log.info("Fetching products by category: {}", category);
            
            List<Product> products = productRepository.findByCategory(category);
            return products;
        } catch (Exception e) {
            log.error("Error fetching products by category", e);
            throw e;
        } finally {
        }
    }
    
    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        try {
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
            log.error("Error updating product", e);
            throw e;
        } finally {
        }
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        try {
            log.info("Deleting product with ID: {}", id);
            
            Product product = getProductById(id);
            productRepository.delete(product);
            
            log.info("Product deleted successfully with ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting product", e);
            throw e;
        } finally {
        }
    }
    
    @Transactional
    public boolean updateStock(Long productId, Integer quantity) {
        try {
            log.info("Updating stock for product ID: {} with quantity: {}", productId, quantity);
            
            Product product = getProductById(productId);
            
            if (product.getStockQuantity() < quantity) {
                log.warn("Insufficient stock for product ID: {}", productId);
                return false;
            }
            
            product.setStockQuantity(product.getStockQuantity() - quantity);
            productRepository.save(product);
            
            log.info("Stock updated successfully for product ID: {}", productId);
            return true;
        } catch (Exception e) {
            log.error("Error updating stock", e);
            throw e;
        } finally {
        }
    }
}
