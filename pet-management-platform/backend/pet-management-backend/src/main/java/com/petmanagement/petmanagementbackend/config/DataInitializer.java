package com.petmanagement.petmanagementbackend.config;

import com.petmanagement.petmanagementbackend.models.ERole;
import com.petmanagement.petmanagementbackend.models.Role;
import com.petmanagement.petmanagementbackend.models.User;
import com.petmanagement.petmanagementbackend.models.UserType;
import com.petmanagement.petmanagementbackend.repository.RoleRepository;
import com.petmanagement.petmanagementbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        System.out.println("=================================");
        System.out.println("开始初始化数据...");
        System.out.println("=================================");
        initRoles();
        createDefaultAdmin();
        System.out.println("=================================");
        System.out.println("数据初始化完成！");
        System.out.println("=================================");
    }

    private void initRoles() {
        System.out.println("正在初始化角色...");
        Arrays.stream(ERole.values()).forEach(eRole -> {
            if (!roleRepository.existsByName(eRole)) {
                Role role = new Role(eRole);
                roleRepository.save(role);
                System.out.println("  ✓ 创建角色: " + eRole.name());
            } else {
                System.out.println("  - 角色已存在: " + eRole.name());
            }
        });
        System.out.println("角色初始化完成！");
    }

    private void createDefaultAdmin() {
        System.out.println("正在检查管理员账号...");
        String adminUsername = "admin";

        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail("admin@petmanagement.com");

            String rawPassword = "admin123";
            // 用 DelegatingPasswordEncoder 进行编码（会写入 {bcrypt} 前缀）
            String encodedPassword = passwordEncoder.encode(rawPassword);
            admin.setPassword(encodedPassword);

            System.out.println("==============================================");
            System.out.println("【创建时】admin 加密密码: " + encodedPassword);
            System.out.println("passwordEncoder.matches('admin123', encoded) = "
                    + passwordEncoder.matches("admin123", encodedPassword));
            System.out.println("==============================================");

            admin.setPhone("13800138000");
            admin.setRealName("系统管理员");
            admin.setUserType(UserType.ADMIN);
            admin.setStatus(1);

            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("角色未找到"));
            admin.setRoles(new HashSet<>(Arrays.asList(adminRole)));

            userRepository.save(admin);
            System.out.println("✓ 默认管理员账号创建成功！");
        } else {
            System.out.println("  - 管理员账号已存在: " + adminUsername);

            // 取出现有 admin，打印密码，帮助排查
            userRepository.findByUsername(adminUsername).ifPresent(u -> {
                System.out.println("【已存在的 admin】密码哈希: " + u.getPassword());
                System.out.println("matches('admin123', dbPassword) = "
                        + passwordEncoder.matches("admin123", u.getPassword()));
            });
        }
    }
}