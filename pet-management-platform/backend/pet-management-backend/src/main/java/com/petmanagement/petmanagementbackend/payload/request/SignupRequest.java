package com.petmanagement.petmanagementbackend.payload.request;

import com.petmanagement.petmanagementbackend.models.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    @Size(max = 50)
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
    
    // 前端传递的角色，如: ["merchant_hospital"]
    private Set<String> role;

    // 用户类型，前端传递 USER 或 MERCHANT
    private UserType userType = UserType.USER;
    
    // 基本信息
    private String phone;
    private String realName;
    
    // 商家信息字段
    private String merchantName;
    private String businessLicense;
    private String address;
}