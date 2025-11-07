package com.petmanagement.petmanagementbackend.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "base_dict_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseDictType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "dict_code", unique = true, nullable = false, length = 50)
    private String dictCode;
    
    @Column(name = "dict_name", nullable = false, length = 100)
    private String dictName;
    
    @Column(name = "dict_level")
    private Integer dictLevel = 1;
    
    @Column(name = "parent_code", length = 50)
    private String parentCode;
    
    @Column(name = "is_fixed")
    private Boolean isFixed = false;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "status")
    private Integer status = 1; // 1-启用 0-禁用
    
    @Column(name = "version")
    private Integer version = 1;
    
    @Column(name = "created_by", length = 50)
    private String createdBy;
    
    @Column(name = "updated_by", length = 50)
    private String updatedBy;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}