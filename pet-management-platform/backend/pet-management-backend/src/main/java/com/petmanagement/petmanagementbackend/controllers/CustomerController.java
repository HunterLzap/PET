package com.petmanagement.petmanagementbackend.controllers;

import com.petmanagement.petmanagementbackend.models.Customer;
import com.petmanagement.petmanagementbackend.payload.response.MessageResponse;
import com.petmanagement.petmanagementbackend.repository.CustomerRepository;
import com.petmanagement.petmanagementbackend.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;

    // 获取我的客户列表（商家）
    @GetMapping
    @PreAuthorize("hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<List<Customer>> getMyCustomers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        List<Customer> customers;
        
        // 管理员可以查看所有客户
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            customers = customerRepository.findAll();
        } else {
            // 商家只能查看自己的客户
            customers = customerRepository.findByMerchantId(userDetails.getId());
        }
        
        return ResponseEntity.ok(customers);
    }

    // 搜索客户
    @GetMapping("/search")
    @PreAuthorize("hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<List<Customer>> searchCustomers(@RequestParam String keyword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        List<Customer> customers = customerRepository
            .findByMerchantIdAndNameContainingOrPhoneContaining(
                userDetails.getId(), keyword, keyword
            );
        
        return ResponseEntity.ok(customers);
    }

    // 根据ID获取客户详情
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<?> getCustomerById(@PathVariable Long id) {
        Optional<Customer> customerData = customerRepository.findById(id);
        
        if (customerData.isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("客户不存在"), HttpStatus.NOT_FOUND);
        }
        
        Customer customer = customerData.get();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // 商家只能查看自己的客户
        if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            if (!customer.getMerchantId().equals(userDetails.getId())) {
                return new ResponseEntity<>(new MessageResponse("无权访问"), HttpStatus.FORBIDDEN);
            }
        }
        
        return ResponseEntity.ok(customer);
    }

    // 创建客户
    @PostMapping
    @PreAuthorize("hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<?> createCustomer(@Valid @RequestBody CustomerRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // 检查手机号是否已存在（同一商家下）
        Optional<Customer> existing = customerRepository
            .findByMerchantIdAndPhone(userDetails.getId(), request.getPhone());
        if (existing.isPresent()) {
            return ResponseEntity
                .badRequest()
                .body(new MessageResponse("该手机号客户已存在"));
        }
        
        Customer customer = new Customer();
        customer.setMerchantId(userDetails.getId());
        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setGender(request.getGender());
        customer.setAddress(request.getAddress());
        customer.setNotes(request.getNotes());
        
        Customer savedCustomer = customerRepository.save(customer);
        return new ResponseEntity<>(savedCustomer, HttpStatus.CREATED);
    }

    // 更新客户信息
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<?> updateCustomer(@PathVariable Long id, 
                                           @Valid @RequestBody CustomerRequest request) {
        Optional<Customer> customerData = customerRepository.findById(id);
        
        if (customerData.isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("客户不存在"), HttpStatus.NOT_FOUND);
        }
        
        Customer customer = customerData.get();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // 商家只能修改自己的客户
        if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            if (!customer.getMerchantId().equals(userDetails.getId())) {
                return new ResponseEntity<>(new MessageResponse("无权访问"), HttpStatus.FORBIDDEN);
            }
        }
        
        customer.setName(request.getName());
        customer.setPhone(request.getPhone());
        customer.setGender(request.getGender());
        customer.setAddress(request.getAddress());
        customer.setNotes(request.getNotes());
        
        Customer updatedCustomer = customerRepository.save(customer);
        return ResponseEntity.ok(updatedCustomer);
    }

    // 删除客户
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT_HOSPITAL') or hasRole('MERCHANT_HOUSE') or hasRole('MERCHANT_GOODS') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteCustomer(@PathVariable Long id) {
        Optional<Customer> customerData = customerRepository.findById(id);
        
        if (customerData.isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("客户不存在"), HttpStatus.NOT_FOUND);
        }
        
        Customer customer = customerData.get();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // 商家只能删除自己的客户
        if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            if (!customer.getMerchantId().equals(userDetails.getId())) {
                return new ResponseEntity<>(new MessageResponse("无权访问"), HttpStatus.FORBIDDEN);
            }
        }
        
        customerRepository.deleteById(id);
        return ResponseEntity.ok(new MessageResponse("客户删除成功"));
    }

    // 请求DTO
    @Data
    static class CustomerRequest {
        private String name;
        private String phone;
        private String gender;
        private String address;
        private String notes;
    }
}