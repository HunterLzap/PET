package com.petmanagement.petmanagementbackend.models;

/**
 * 角色枚举（对应需求中的三端权限）
 */
public enum ERole {
    // ========== 用户端 ==========
    /**
     * 普通用户（宠物主人）
     */
    ROLE_USER,
    
    // ========== 商家端 ==========
    /**
     * 宠物医院
     */
    ROLE_MERCHANT_HOSPITAL,
    
    /**
     * 宠物馆（提供寄养、美容、训练服务）
     */
    ROLE_MERCHANT_HOUSE,
    
    /**
     * 用品商家
     */
    ROLE_MERCHANT_GOODS,
    
    // ========== 平台管理端 ==========
    /**
     * 超级管理员（所有权限）
     */
    ROLE_ADMIN,
    
    /**
     * 数据管理员（管理基础数据）
     */
    ROLE_DATA_MANAGER,
    
    /**
     * 运营管理员（内容审核、用户管理等）
     */
    ROLE_OPERATOR;
    
    /**
     * 获取角色的中文描述
     */
    public String getDescription() {
        switch (this) {
            case ROLE_USER:
                return "宠物主人";
            case ROLE_MERCHANT_HOSPITAL:
                return "宠物医院";
            case ROLE_MERCHANT_HOUSE:
                return "宠物馆";
            case ROLE_MERCHANT_GOODS:
                return "用品商家";
            case ROLE_ADMIN:
                return "超级管理员";
            case ROLE_DATA_MANAGER:
                return "数据管理员";
            case ROLE_OPERATOR:
                return "运营管理员";
            default:
                return this.name();
        }
    }
    
    /**
     * 判断是否是商家角色
     */
    public boolean isMerchantRole() {
        return this == ROLE_MERCHANT_HOSPITAL 
            || this == ROLE_MERCHANT_HOUSE 
            || this == ROLE_MERCHANT_GOODS;
    }
    
    /**
     * 判断是否是管理员角色
     */
    public boolean isAdminRole() {
        return this == ROLE_ADMIN 
            || this == ROLE_DATA_MANAGER 
            || this == ROLE_OPERATOR;
    }
}