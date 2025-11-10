package com.petmanagement.petmanagementbackend.controllers;

import com.petmanagement.petmanagementbackend.models.Order;
import com.petmanagement.petmanagementbackend.models.OrderStatus;
import com.petmanagement.petmanagementbackend.models.PaymentStatus;
import com.petmanagement.petmanagementbackend.payload.response.MessageResponse;
import com.petmanagement.petmanagementbackend.repository.OrderRepository;
import com.petmanagement.petmanagementbackend.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    OrderRepository orderRepository;

    // 获取所有订单（管理员）或当前用户的订单
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getAllOrders() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            List<Order> orders;
            
            // 管理员可以查看所有订单
            if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                orders = orderRepository.findAll();
            } 
            // 商家查看自己作为服务方的订单
            else if (authentication.getAuthorities().stream().anyMatch(a -> 
                    a.getAuthority().equals("ROLE_MERCHANT_HOSPITAL") ||
                    a.getAuthority().equals("ROLE_MERCHANT_HOUSE") ||
                    a.getAuthority().equals("ROLE_MERCHANT_GOODS"))) {
                orders = orderRepository.findByBusinessId(userDetails.getId());
            }
            // 普通用户查看自己的订单
            else {
                orders = orderRepository.findByUserId(userDetails.getId());
            }
            
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 根据ID获取订单
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<Order> getOrderById(@PathVariable("id") Long id) {
        Optional<Order> orderData = orderRepository.findById(id);

        if (orderData.isPresent()) {
            Order order = orderData.get();
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 检查权限：只能查看自己的订单（除非是管理员）
            if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                if (!order.getUserId().equals(userDetails.getId()) && 
                    !order.getBusinessId().equals(userDetails.getId())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }

            return new ResponseEntity<>(order, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 创建订单
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<?> createOrder(@Valid @RequestBody Order order) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 设置创建者为当前用户
            order.setUserId(userDetails.getId());
            
            // 设置初始状态
            if (order.getStatus() == null) {
                order.setStatus(OrderStatus.PENDING);
            }
            if (order.getPaymentStatus() == null) {
                order.setPaymentStatus(PaymentStatus.PENDING);
            }

            Order savedOrder = orderRepository.save(order);
            return new ResponseEntity<>(savedOrder, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new MessageResponse("Error: Failed to create order! " + e.getMessage()), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 更新订单
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<?> updateOrder(@PathVariable("id") Long id, @Valid @RequestBody Order order) {
        Optional<Order> orderData = orderRepository.findById(id);

        if (orderData.isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("Error: Order not found!"), HttpStatus.NOT_FOUND);
        }

        try {
            Order existingOrder = orderData.get();
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 检查权限
            if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                if (!existingOrder.getUserId().equals(userDetails.getId()) && 
                    !existingOrder.getBusinessId().equals(userDetails.getId())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }

            // 更新字段
            existingOrder.setServiceId(order.getServiceId());
            existingOrder.setPetId(order.getPetId());
            existingOrder.setTotalAmount(order.getTotalAmount());
            existingOrder.setStatus(order.getStatus());
            existingOrder.setPaymentStatus(order.getPaymentStatus());
            existingOrder.setAppointmentTime(order.getAppointmentTime());
            existingOrder.setRemarks(order.getRemarks());

            Order updatedOrder = orderRepository.save(existingOrder);
            return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new MessageResponse("Error: Failed to update order!"), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 删除订单
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteOrder(@PathVariable("id") Long id) {
        try {
            Optional<Order> orderData = orderRepository.findById(id);
            
            if (orderData.isEmpty()) {
                return new ResponseEntity<>(new MessageResponse("Error: Order not found!"), HttpStatus.NOT_FOUND);
            }

            Order order = orderData.get();
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            // 只有创建者或管理员可以删除
            if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                if (!order.getUserId().equals(userDetails.getId())) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }
            }

            orderRepository.deleteById(id);
            return new ResponseEntity<>(new MessageResponse("Order deleted successfully!"), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new MessageResponse("Error: Failed to delete order!"), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}