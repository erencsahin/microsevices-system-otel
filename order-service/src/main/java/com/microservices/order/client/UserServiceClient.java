package com.microservices.order.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {
    
    private final WebClient.Builder webClientBuilder;
    
    @Value("${services.user.url}")
    private String userServiceUrl;
    
    public boolean verifyUser(Long userId) {
        try {
            log.info("Verifying user with ID: {}", userId);
            
            WebClient webClient = webClientBuilder.baseUrl(userServiceUrl).build();
            
            Map<String, Object> response = webClient.get()
                    .uri("/api/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorResume(e -> {
                        log.error("Error verifying user: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();
            
            boolean verified = response != null && response.get("id") != null;
            log.info("User verification result for ID {}: {}", userId, verified);
            
            return verified;
        } catch (Exception e) {
            log.error("Exception verifying user", e);
            return false;
        }
    }
}
