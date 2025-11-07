package com.petmanagement.petmanagementbackend.models;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users", 
       uniqueConstraints = { 
           @UniqueConstraint(columnNames = "username"),
           @UniqueConstraint(columnNames = "email"),
           @UniqueConstraint(columnNames = "phone") // 新增手机号唯一约束
       })
public class User {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Size(max = 20)
  @Column(name = "username", nullable = false)
  private String username;

  @NotBlank
  @Size(max = 50)
  @Email
  @Column(name = "email", nullable = false)
  private String email;

  @NotBlank
  @Size(max = 120)
  @Column(name = "password", nullable = false)
  private String password;

  // ========== 新增字段 ==========
  
  /**
   * 手机号（重要：用于短信通知、登录）
   */
  @Size(max = 20)
  @Column(name = "phone", length = 20)
  private String phone;
  
  /**
   * 真实姓名
   */
  @Size(max = 50)
  @Column(name = "real_name", length = 50)
  private String realName;
  
  /**
   * 用户类型：区分三大端
   * USER - 用户端（宠物主人）
   * MERCHANT - 商家端
   * ADMIN - 平台管理端
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "user_type", length = 20, nullable = false)
  private UserType userType = UserType.USER;
  
  /**
   * 账号状态
   * 1-正常 0-禁用 2-待审核（商家注册后需审核）
   */
  @Column(name = "status")
  private Integer status = 1;
  
  /**
   * 头像URL
   */
  @Column(name = "avatar_url", length = 255)
  private String avatarUrl;
  
  /**
   * 常用地址（收货地址/寄养地址）JSON格式
   * 示例：[{"province":"江苏","city":"南京","detail":"...","isDefault":true}]
   */
  @Column(name = "addresses", columnDefinition = "TEXT")
  private String addresses;
  
  /**
   * 最后登录时间
   */
  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;
  
  /**
   * 最后登录IP
   */
  @Column(name = "last_login_ip", length = 50)
  private String lastLoginIp;
  
  /**
   * 账号创建时间
   */
  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
  
  /**
   * 账号更新时间
   */
  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // ========== 原有的角色关系 ==========
  
  @ManyToMany(fetch = FetchType.EAGER) // 改为EAGER，登录时需要立即获取角色
  @JoinTable(name = "user_roles", 
             joinColumns = @JoinColumn(name = "user_id"), 
             inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  // ========== 构造函数 ==========
  
  public User() {
  }

  public User(String username, String email, String password) {
    this.username = username;
    this.email = email;
    this.password = password;
  }
  
  /**
   * 完整构造函数
   */
  public User(String username, String email, String password, String phone, UserType userType) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.phone = phone;
    this.userType = userType;
  }

  // ========== Getter & Setter ==========
  
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Set<Role> getRoles() {
    return roles;
  }

  public void setRoles(Set<Role> roles) {
    this.roles = roles;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getRealName() {
    return realName;
  }

  public void setRealName(String realName) {
    this.realName = realName;
  }

  public UserType getUserType() {
    return userType;
  }

  public void setUserType(UserType userType) {
    this.userType = userType;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  public String getAddresses() {
    return addresses;
  }

  public void setAddresses(String addresses) {
    this.addresses = addresses;
  }

  public LocalDateTime getLastLoginAt() {
    return lastLoginAt;
  }

  public void setLastLoginAt(LocalDateTime lastLoginAt) {
    this.lastLoginAt = lastLoginAt;
  }

  public String getLastLoginIp() {
    return lastLoginIp;
  }

  public void setLastLoginIp(String lastLoginIp) {
    this.lastLoginIp = lastLoginIp;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
  
  // ========== 业务方法 ==========
  
  /**
   * 判断是否是普通用户
   */
  public boolean isUser() {
    return UserType.USER.equals(this.userType);
  }
  
  /**
   * 判断是否是商家
   */
  public boolean isMerchant() {
    return UserType.MERCHANT.equals(this.userType);
  }
  
  /**
   * 判断是否是管理员
   */
  public boolean isAdmin() {
    return UserType.ADMIN.equals(this.userType);
  }
  
  /**
   * 判断是否拥有某个角色
   */
  public boolean hasRole(ERole roleName) {
    return this.roles.stream()
        .anyMatch(role -> role.getName().equals(roleName));
  }
  
  /**
   * 添加角色
   */
  public void addRole(Role role) {
    this.roles.add(role);
  }
  
  /**
   * 移除角色
   */
  public void removeRole(Role role) {
    this.roles.remove(role);
  }
}