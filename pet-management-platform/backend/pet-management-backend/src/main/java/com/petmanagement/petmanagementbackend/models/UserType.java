package com.petmanagement.petmanagementbackend.models;

/**
 * 用户类型枚举
 * 用于区分三大端：用户端、商家端、平台管理端
 */
public enum UserType {
    /**
     * 普通用户（宠物主人）
     */
    USER("普通用户"),
    
    /**
     * 商家（宠物医院/宠物馆/用品商家）
     */
    MERCHANT("商家"),
    
    /**
     * 平台管理员
     */
    ADMIN("平台管理员");
    
    private final String description;
    
    UserType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}