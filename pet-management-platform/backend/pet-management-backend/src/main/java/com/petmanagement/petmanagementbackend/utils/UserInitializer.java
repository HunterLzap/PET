package com.petmanagement.petmanagementbackend.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.petmanagement.petmanagementbackend.models.User;
import com.petmanagement.petmanagementbackend.models.ERole;
import com.petmanagement.petmanagementbackend.models.Role;
import com.petmanagement.petmanagementbackend.repository.UserRepository;
import com.petmanagement.petmanagementbackend.repository.RoleRepository;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 初始化用户数据的CommandLineRunner
 * 在应用启动时自动创建或更新管理员和普通用户
 */
@Component
public class UserInitializer implements CommandLineRunner {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Override
    public void run(String... args) throws Exception {
        // 强制创建或更新管理员用户
        User adminUser;
        Optional<User> existingAdmin = userRepository.findByUsername("admin");
        
        if (existingAdmin.isPresent()) {
            // 如果用户存在，更新密码和角色
            adminUser = existingAdmin.get();
            adminUser.setPassword(encoder.encode("admin123"));
            adminUser.setEmail("admin@example.com");
            System.out.println("管理员用户已更新：用户名admin，密码admin123");
        } else {
            // 如果用户不存在，创建新用户
            adminUser = new User("admin", "admin@example.com", encoder.encode("admin123"));
            System.out.println("管理员用户已创建：用户名admin，密码admin123");
        }
        
        // 设置管理员角色
        Set<Role> adminRoles = new HashSet<>();
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Error: 管理员角色不存在。"));
        adminRoles.add(adminRole);
        adminUser.setRoles(adminRoles);
        userRepository.save(adminUser);
        
        // 强制创建或更新普通用户
        User normalUser;
        Optional<User> existingUser = userRepository.findByUsername("user");
        
        if (existingUser.isPresent()) {
            // 如果用户存在，更新密码和角色
            normalUser = existingUser.get();
            normalUser.setPassword(encoder.encode("user123"));
            normalUser.setEmail("user@example.com");
            System.out.println("普通用户已更新：用户名user，密码user123");
        } else {
            // 如果用户不存在，创建新用户
            normalUser = new User("user", "user@example.com", encoder.encode("user123"));
            System.out.println("普通用户已创建：用户名user，密码user123");
        }
        
        // 设置普通用户角色
        Set<Role> userRoles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_PET_OWNER)
                .orElseThrow(() -> new RuntimeException("Error: 用户角色不存在。"));
        userRoles.add(userRole);
        normalUser.setRoles(userRoles);
        userRepository.save(normalUser);
    }
}