package com.petmanagement.petmanagementbackend.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AddPetRequest {
    
    // ✅ 新增：客户ID（商家添加宠物时使用）
    private Long customerId;
    
    @NotBlank(message = "宠物名称不能为空")
    private String name;
    
    @NotBlank(message = "宠物种类不能为空")
    private String speciesCode;
    
    @NotBlank(message = "宠物品种不能为空")
    private String breedCode;
    
    @NotBlank(message = "宠物性别不能为空")
    private String genderCode;
    
    private LocalDate birthDate;
    private BigDecimal weight;
    private String color;
    private String description;
    private String avatarUrl;
}