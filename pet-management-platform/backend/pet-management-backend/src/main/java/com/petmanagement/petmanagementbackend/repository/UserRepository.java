package com.petmanagement.petmanagementbackend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.petmanagement.petmanagementbackend.models.User;
import com.petmanagement.petmanagementbackend.models.UserType;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 检查用户名是否存在
     */
    Boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    Boolean existsByEmail(String email);
    
    // ========== 新增方法（适配 User 实体的新字段）==========
    
    /**
     * 根据手机号查找用户
     */
    Optional<User> findByPhone(String phone);
    
    /**
     * 检查手机号是否存在
     */
    Boolean existsByPhone(String phone);
    
    /**
     * 根据用户类型查询用户列表
     */
    java.util.List<User> findByUserType(UserType userType);
    
    /**
     * 根据状态查询用户（用于查询待审核的商家）
     */
    java.util.List<User> findByStatus(Integer status);
    
    /**
     * 根据用户类型和状态查询
     */
    java.util.List<User> findByUserTypeAndStatus(UserType userType, Integer status);
}