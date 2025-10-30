package com.microservices.user.service;

import com.microservices.user.model.User;
import com.microservices.user.repository.UserRepository;
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
public class UserService {
    
    private final UserRepository userRepository;
    private final Tracer tracer;
    
    @Transactional
    public User createUser(User user) {
        Span span = tracer.spanBuilder("createUser").startSpan();
        try {
            log.info("Creating new user with email: {}", user.getEmail());
            
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new RuntimeException("User with email " + user.getEmail() + " already exists");
            }
            
            User savedUser = userRepository.save(user);
            span.setAttribute("user.id", savedUser.getId());
            span.setAttribute("user.email", savedUser.getEmail());
            
            log.info("User created successfully with ID: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            span.recordException(e);
            log.error("Error creating user", e);
            throw e;
        } finally {
            span.end();
        }
    }
    
    public User getUserById(Long id) {
        Span span = tracer.spanBuilder("getUserById").startSpan();
        try {
            span.setAttribute("user.id", id);
            log.info("Fetching user with ID: {}", id);
            
            return userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        } catch (Exception e) {
            span.recordException(e);
            log.error("Error fetching user", e);
            throw e;
        } finally {
            span.end();
        }
    }
    
    public List<User> getAllUsers() {
        Span span = tracer.spanBuilder("getAllUsers").startSpan();
        try {
            log.info("Fetching all users");
            List<User> users = userRepository.findAll();
            span.setAttribute("users.count", users.size());
            return users;
        } catch (Exception e) {
            span.recordException(e);
            log.error("Error fetching all users", e);
            throw e;
        } finally {
            span.end();
        }
    }
    
    @Transactional
    public User updateUser(Long id, User userDetails) {
        Span span = tracer.spanBuilder("updateUser").startSpan();
        try {
            span.setAttribute("user.id", id);
            log.info("Updating user with ID: {}", id);
            
            User user = getUserById(id);
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            user.setPhoneNumber(userDetails.getPhoneNumber());
            
            User updatedUser = userRepository.save(user);
            log.info("User updated successfully with ID: {}", id);
            return updatedUser;
        } catch (Exception e) {
            span.recordException(e);
            log.error("Error updating user", e);
            throw e;
        } finally {
            span.end();
        }
    }
    
    @Transactional
    public void deleteUser(Long id) {
        Span span = tracer.spanBuilder("deleteUser").startSpan();
        try {
            span.setAttribute("user.id", id);
            log.info("Deleting user with ID: {}", id);
            
            User user = getUserById(id);
            userRepository.delete(user);
            
            log.info("User deleted successfully with ID: {}", id);
        } catch (Exception e) {
            span.recordException(e);
            log.error("Error deleting user", e);
            throw e;
        } finally {
            span.end();
        }
    }
}
