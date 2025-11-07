package com.petmanagement.petmanagementbackend.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 宠物信息响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PetResponse {
    
    /**
     * 宠物ID
     */
    private Long id;
    
    /**
     * 宠物名称
     */
    private String name;
    
    /**
     * 宠物种类编码
     */
    private String speciesCode;
    
    /**
     * 宠物种类名称（冗余字段，方便前端显示）
     */
    private String speciesName;
    
    /**
     * 宠物品种编码
     */
    private String breedCode;
    
    /**
     * 宠物品种名称（冗余字段）
     */
    private String breedName;
    
    /**
     * 性别编码
     */
    private String genderCode;
    
    /**
     * 性别名称（冗余字段）
     */
    private String genderName;
    
    /**
     * 出生日期
     */
    private LocalDate birthDate;
    
    /**
     * 年龄（计算得出，单位：月）
     */
    private Integer ageInMonths;
    
    /**
     * 体重（kg）
     */
    private BigDecimal weight;
    
    /**
     * 毛色
     */
    private String color;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}