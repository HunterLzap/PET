package com.petmanagement.petmanagementbackend.models;

public enum UserRole {
    // 用户端
    ROLE_USER("宠物主人"),
    
    // 商家端
    ROLE_MERCHANT_HOSPITAL("宠物医院"),
    ROLE_MERCHANT_HOUSE("宠物馆"),
    ROLE_MERCHANT_GOODS("用品商家"),
    
    // 平台管理端
    ROLE_ADMIN("超级管理员"),
    ROLE_DATA_MANAGER("数据管理员"),
    ROLE_OPERATOR("运营管理员");
    
    private final String description;
    
    UserRole(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}