package com.petmanagement.petmanagementbackend.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 更新宠物请求 DTO
 */
@Data
public class UpdatePetRequest {
    
    /**
     * 宠物名称
     */
    @NotBlank(message = "宠物名称不能为空")
    @Size(max = 50, message = "宠物名称不能超过50个字符")
    private String name;
    
    /**
     * 宠物品种编码（允许修改品种）
     */
    @NotBlank(message = "请选择宠物品种")
    private String breedCode;
    
    /**
     * 性别编码
     */
    @NotBlank(message = "请选择宠物性别")
    private String genderCode;
    
    /**
     * 出生日期
     */
    private LocalDate birthDate;
    
    /**
     * 体重（kg）
     */
    private BigDecimal weight;
    
    /**
     * 毛色
     */
    @Size(max = 50, message = "毛色描述不能超过50个字符")
    private String color;
    
    /**
     * 描述
     */
    @Size(max = 500, message = "描述不能超过500个字符")
    private String description;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
}