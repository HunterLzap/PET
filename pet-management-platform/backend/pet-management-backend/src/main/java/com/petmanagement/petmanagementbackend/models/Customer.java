package com.petmanagement.petmanagementbackend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;
    
    @Column(name = "name", nullable = false, length = 50)
    private String name;
    
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;
    
    @Column(name = "user_id")
    private Long userId;  // 关联系统用户（客户注册后绑定）
    
    @Column(name = "gender", length = 10)
    private String gender;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "total_orders")
    private Integer totalOrders = 0;
    
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(name = "last_visit_at")
    private LocalDateTime lastVisitAt;
    
    @Column(name = "status")
    private Integer status = 1;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}