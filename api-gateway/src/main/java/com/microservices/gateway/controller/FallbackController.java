package com.microservices.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {
    
    @GetMapping("/users")
    public ResponseEntity<Map<String, String>> userServiceFallback() {
        log.warn("User Service is unavailable - returning fallback response");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "message", "User Service is currently unavailable. Please try again later.",
                        "service", "user-service"
                ));
    }
    
    @GetMapping("/products")
    public ResponseEntity<Map<String, String>> productServiceFallback() {
        log.warn("Product Service is unavailable - returning fallback response");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "message", "Product Service is currently unavailable. Please try again later.",
                        "service", "product-service"
                ));
    }
    
    @GetMapping("/orders")
    public ResponseEntity<Map<String, String>> orderServiceFallback() {
        log.warn("Order Service is unavailable - returning fallback response");
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "message", "Order Service is currently unavailable. Please try again later.",
                        "service", "order-service"
                ));
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("API Gateway is running!");
    }
}
