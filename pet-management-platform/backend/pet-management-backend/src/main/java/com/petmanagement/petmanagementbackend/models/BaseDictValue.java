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
@Table(name = "base_dict_value",
       uniqueConstraints = @UniqueConstraint(columnNames = {"dict_code", "value_code"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseDictValue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "dict_code", nullable = false, length = 50)
    private String dictCode;
    
    @Column(name = "value_code", nullable = false, length = 50)
    private String valueCode;
    
    @Column(name = "value_name", nullable = false, length = 100)
    private String valueName;
    
    @Column(name = "value_order")
    private Integer valueOrder = 0;
    
    @Column(name = "extra_data", columnDefinition = "JSON")
    private String extraData;     
    @Column(name = "color_tag", length = 20)
    private String colorTag;
    
    @Column(name = "icon", length = 100)
    private String icon;
    
    @Column(name = "status")
    private Integer status = 1;
    
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