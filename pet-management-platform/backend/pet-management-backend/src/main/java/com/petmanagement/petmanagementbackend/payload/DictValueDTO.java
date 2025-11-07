package com.petmanagement.petmanagementbackend.payload;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DictValueDTO {
    
    private Long id;
    
    @NotBlank(message = "字典编码不能为空")
    private String dictCode;
    
    @NotBlank(message = "值编码不能为空")
    private String valueCode;
    
    @NotBlank(message = "值名称不能为空")
    private String valueName;
    
    private Integer valueOrder;
    
    private String extraData; // JSON 字符串
    
    private String colorTag;
    
    private Integer status;
    
    private String operationReason; // 操作原因（用于版本记录）
}