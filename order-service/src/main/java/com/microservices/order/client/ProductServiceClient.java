package com.microservices.order.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClient {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${services.product.url}")
    private String productServiceUrl;
    
    public Map<String, Object> getProduct(Long productId) {
        try {
            log.info("Fetching product with ID: {}", productId);
            
            WebClient webClient = webClientBuilder.baseUrl(productServiceUrl).build();
            
            Map<String, Object> product = webClient.get()
                    .uri("/api/products/{id}", productId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(e -> {
                        log.error("Error fetching product: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();
            
            log.info("Product fetched: {}", product != null ? product.get("name") : "null");
            return product;
        } catch (Exception e) {
            log.error("Exception fetching product", e);
            return null;
        }
    }
    
    public boolean updateStock(Long productId, Integer quantity) {
        try {
            log.info("Updating stock for product ID: {} with quantity: {}", productId, quantity);
            
            WebClient webClient = webClientBuilder.baseUrl(productServiceUrl).build();
            
            Map<String, Object> response = webClient.post()
                    .uri("/api/products/{id}/stock", productId)
                    .bodyValue(Map.of("quantity", quantity))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(e -> {
                        log.error("Error updating stock: {}", e.getMessage());
                        return Mono.just(Map.of("success", false));
                    })
                    .block();
            
            boolean success = response != null && Boolean.TRUE.equals(response.get("success"));
            log.info("Stock update result for product ID {}: {}", productId, success);
            
            return success;
        } catch (Exception e) {
            log.error("Exception updating stock", e);
            return false;
        }
    }
}
