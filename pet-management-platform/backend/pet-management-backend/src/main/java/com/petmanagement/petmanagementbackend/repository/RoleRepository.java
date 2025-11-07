package com.petmanagement.petmanagementbackend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.petmanagement.petmanagementbackend.models.ERole;
import com.petmanagement.petmanagementbackend.models.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    /**
     * 根据角色名称查找角色
     */
    Optional<Role> findByName(ERole name);
    
    /**
     * 检查角色是否存在（新增，用于数据初始化）
     */
    Boolean existsByName(ERole name);
}