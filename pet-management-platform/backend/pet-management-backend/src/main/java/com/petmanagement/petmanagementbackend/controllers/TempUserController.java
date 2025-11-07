package com.petmanagement.petmanagementbackend.controllers;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.petmanagement.petmanagementbackend.models.ERole;
import com.petmanagement.petmanagementbackend.models.Role;
import com.petmanagement.petmanagementbackend.models.User;
import com.petmanagement.petmanagementbackend.models.UserType;
import com.petmanagement.petmanagementbackend.payload.response.MessageResponse;
import com.petmanagement.petmanagementbackend.repository.RoleRepository;
import com.petmanagement.petmanagementbackend.repository.UserRepository;

/**
 * 临时用户创建控制器
 * 注意：仅用于开发测试，生产环境应删除此文件！
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/temp")
public class TempUserController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    /**
     * 创建管理员用户
     * POST http://localhost:8081/api/temp/create-admin
     */
    @PostMapping("/create-admin")
    public ResponseEntity<?> createAdminUser() {
        // 检查用户是否已存在
        if (userRepository.existsByUsername("admin")) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: 管理员用户已存在！"));
        }

        // 创建新用户
        User user = new User("admin",
                "admin@example.com",
                encoder.encode("admin123"));

        // 设置用户类型
        user.setUserType(UserType.ADMIN);
        user.setStatus(1); // 正常状态

        // 分配角色
        Set<Role> roles = new HashSet<>();
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Error: 管理员角色不存在。"));
        roles.add(adminRole);
        user.setRoles(roles);

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("管理员用户创建成功！用户名：admin，密码：admin123"));
    }

    /**
     * 创建普通用户（宠物主人）
     * POST http://localhost:8081/api/temp/create-user
     */
    @PostMapping("/create-user")
    public ResponseEntity<?> createUser() {
        if (userRepository.existsByUsername("user")) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: 用户已存在！"));
        }

        User user = new User("user",
                "user@example.com",
                encoder.encode("user123"));

        // 设置用户类型
        user.setUserType(UserType.USER);
        user.setStatus(1); // 正常状态

        // 分配角色（改为 ROLE_USER）
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: 用户角色不存在。"));
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("用户创建成功！用户名：user，密码：user123"));
    }

    /**
     * 创建商家用户（宠物医院）
     * POST http://localhost:8081/api/temp/create-merchant
     */
    @PostMapping("/create-merchant")
    public ResponseEntity<?> createMerchant() {
        if (userRepository.existsByUsername("merchant")) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: 商家用户已存在！"));
        }

        User user = new User("merchant",
                "merchant@example.com",
                encoder.encode("merchant123"));

        // 设置用户类型
        user.setUserType(UserType.MERCHANT);
        user.setStatus(1); // 直接设为正常状态（测试用，实际应该是2待审核）

        // 分配角色
        Set<Role> roles = new HashSet<>();
        Role merchantRole = roleRepository.findByName(ERole.ROLE_MERCHANT_HOSPITAL)
                .orElseThrow(() -> new RuntimeException("Error: 商家角色不存在。"));
        roles.add(merchantRole);
        user.setRoles(roles);

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("商家用户创建成功！用户名：merchant，密码：merchant123"));
    }
}