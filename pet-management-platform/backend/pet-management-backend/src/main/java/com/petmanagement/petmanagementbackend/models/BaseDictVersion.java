package com.petmanagement.petmanagementbackend.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "base_data_version") // 复用现有表，只需修改映射
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseDictVersion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "base_data_id")
    private Long baseDataId; // 兼容旧字段
    
    @Column(name = "dict_code", length = 50)
    private String dictCode;
    
    @Column(name = "value_code", length = 50)
    private String valueCode;
    
    @Column(name = "version")
    private Integer version;
    
    @Column(name = "snapshot", columnDefinition = "JSON")
    private String snapshot; // JSON 格式的数据快照
    
    @Column(name = "action", length = 50)
    private String action; // ADD/UPDATE/DISABLE/ENABLE/ROLLBACK
    
    @Column(name = "operated_by", length = 50)
    private String operatedBy;
    
    @Column(name = "operated_at")
    private LocalDateTime operatedAt;
    
    @Column(name = "operation_reason", length = 500)
    private String operationReason;
}