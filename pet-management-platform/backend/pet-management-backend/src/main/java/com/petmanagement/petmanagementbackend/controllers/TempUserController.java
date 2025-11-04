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
import com.petmanagement.petmanagementbackend.payload.response.MessageResponse;
import com.petmanagement.petmanagementbackend.repository.RoleRepository;
import com.petmanagement.petmanagementbackend.repository.UserRepository;

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

    // 临时端点，用于创建一个新的管理员用户
    // 注意：在生产环境中应删除此端点
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

        Set<Role> roles = new HashSet<>();
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Error: 管理员角色不存在。"));
        roles.add(adminRole);
        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("管理员用户创建成功！用户名：admin，密码：admin123"));
    }

    // 创建普通用户
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

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(ERole.ROLE_PET_OWNER)
                .orElseThrow(() -> new RuntimeException("Error: 用户角色不存在。"));
        roles.add(userRole);
        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("用户创建成功！用户名：user，密码：user123"));
    }
}