package com.microservices.order.service;

import com.microservices.order.client.ProductServiceClient;
import com.microservices.order.client.UserServiceClient;
import com.microservices.order.dto.CreateOrderRequest;
import com.microservices.order.model.Order;
import com.microservices.order.model.OrderItem;
import com.microservices.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        try {
            log.info("Creating order for user ID: {}", request.getUserId());
            
            // Verify user exists
            if (!userServiceClient.verifyUser(request.getUserId())) {
                throw new RuntimeException("User not found with ID: " + request.getUserId());
            }
            
            Order order = new Order();
            order.setUserId(request.getUserId());
            order.setStatus(Order.OrderStatus.PENDING);
            
            BigDecimal totalAmount = BigDecimal.ZERO;
            
            // Process each order item
            for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
                try {
                    log.info("Processing order item - Product ID: {}, Quantity: {}",
                            itemRequest.getProductId(), itemRequest.getQuantity());
                    
                    // Get product details
                    Map<String, Object> product = productServiceClient.getProduct(itemRequest.getProductId());
                    if (product == null) {
                        throw new RuntimeException("Product not found with ID: " + itemRequest.getProductId());
                    }
                    
                    // Check and update stock
                    boolean stockUpdated = productServiceClient.updateStock(
                            itemRequest.getProductId(), 
                            itemRequest.getQuantity()
                    );
                    
                    if (!stockUpdated) {
                        throw new RuntimeException("Insufficient stock for product ID: " + itemRequest.getProductId());
                    }
                    
                    // Create order item
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProductId(itemRequest.getProductId());
                    orderItem.setProductName((String) product.get("name"));
                    orderItem.setQuantity(itemRequest.getQuantity());
                    
                    // Parse price from product
                    Object priceObj = product.get("price");
                    BigDecimal price = priceObj instanceof Number 
                            ? BigDecimal.valueOf(((Number) priceObj).doubleValue())
                            : new BigDecimal(priceObj.toString());
                    
                    orderItem.setPrice(price);
                    orderItem.calculateSubtotal();
                    
                    order.addOrderItem(orderItem);
                    totalAmount = totalAmount.add(orderItem.getSubtotal());
                    
                    log.info("Order item processed successfully");
                } catch (Exception e) {
                    throw e;
                }
            }
            
            order.setTotalAmount(totalAmount);
            order.setStatus(Order.OrderStatus.CONFIRMED);
            
            Order savedOrder = orderRepository.save(order);
            

            log.info("Order created successfully with ID: {} and total amount: {}", 
                    savedOrder.getId(), savedOrder.getTotalAmount());
            
            return savedOrder;
        } catch (Exception e) {
            log.error("Error creating order", e);
            throw e;
        }
    }
    
    public Order getOrderById(Long id) {
        try {
            log.info("Fetching order with ID: {}", id);
            
            return orderRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
        } catch (Exception e) {
            log.error("Error fetching order", e);
            throw e;
        }
    }
    
    public List<Order> getAllOrders() {
        try {
            log.info("Fetching all orders");
            List<Order> orders = orderRepository.findAll();
            return orders;
        } catch (Exception e) {
            log.error("Error fetching all orders", e);
            throw e;
        }
    }
    
    public List<Order> getOrdersByUserId(Long userId) {
        try {
            log.info("Fetching orders for user ID: {}", userId);
            
            List<Order> orders = orderRepository.findByUserId(userId);
            return orders;
        } catch (Exception e) {
            log.error("Error fetching orders by user ID", e);
            throw e;
        }
    }
    
    @Transactional
    public Order updateOrderStatus(Long id, Order.OrderStatus status) {
        try {
            log.info("Updating order status for ID: {} to {}", id, status);
            
            Order order = getOrderById(id);
            order.setStatus(status);
            
            Order updatedOrder = orderRepository.save(order);
            log.info("Order status updated successfully");
            
            return updatedOrder;
        } catch (Exception e) {
            log.error("Error updating order status", e);
            throw e;
        }
    }
}
