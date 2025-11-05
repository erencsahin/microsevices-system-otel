package com.microservices.user.service;

import com.microservices.user.model.User;
import com.microservices.user.repository.UserRepository;
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

    @Transactional
    public User createUser(User user) {
        try {
            log.info("Creating new user with email: {}", user.getEmail());
            
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new RuntimeException("User with email " + user.getEmail() + " already exists");
            }
            
            User savedUser = userRepository.save(user);

            log.info("User created successfully with ID: {}", savedUser.getId());
            return savedUser;
        } catch (Exception e) {
            log.error("Error creating user", e);
            throw e;
        }
    }
    
    public User getUserById(Long id) {
        try {
            log.info("Fetching user with ID: {}", id);
            
            return userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        } catch (Exception e) {
            log.error("Error fetching user", e);
            throw e;
        } finally {
        }
    }
    
    public List<User> getAllUsers() {
        try {
            log.info("Fetching all users");
            List<User> users = userRepository.findAll();
            return users;
        } catch (Exception e) {
            log.error("Error fetching all users", e);
            throw e;
        }
    }
    
    @Transactional
    public User updateUser(Long id, User userDetails) {
        try {
            log.info("Updating user with ID: {}", id);
            
            User user = getUserById(id);
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            user.setPhoneNumber(userDetails.getPhoneNumber());
            
            User updatedUser = userRepository.save(user);
            log.info("User updated successfully with ID: {}", id);
            return updatedUser;
        } catch (Exception e) {
            log.error("Error updating user", e);
            throw e;
        }
    }
    
    @Transactional
    public void deleteUser(Long id) {
        try {
            log.info("Deleting user with ID: {}", id);
            
            User user = getUserById(id);
            userRepository.delete(user);
            
            log.info("User deleted successfully with ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting user", e);
            throw e;
        }
    }
}
