package com.petmanagement.petmanagementbackend.controllers;

import com.petmanagement.petmanagementbackend.models.ERole;
import com.petmanagement.petmanagementbackend.models.Role;
import com.petmanagement.petmanagementbackend.models.User;
import com.petmanagement.petmanagementbackend.models.UserType;
import com.petmanagement.petmanagementbackend.payload.response.MessageResponse;
import com.petmanagement.petmanagementbackend.repository.RoleRepository;
import com.petmanagement.petmanagementbackend.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // 获取所有用户
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserVO>> getAllUsers() {
        List<UserVO> list = userRepository.findAll().stream()
                .map(UserVO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // 根据ID获取用户
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserVO> getUserById(@PathVariable("id") Long id) {
        Optional<User> userData = userRepository.findById(id);
        return userData.map(user -> new ResponseEntity<>(UserVO.from(user), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 创建用户
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest request) {
        // 检查用户名是否存在
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        // 检查邮箱是否存在
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        try {
            // 创建用户
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRealName(request.getRealName());
            user.setPhone(request.getPhone());
            
            // 设置用户类型
            if (request.getUserType() != null) {
                user.setUserType(UserType.valueOf(request.getUserType()));
            }
            
            // 设置状态
            user.setStatus(request.getStatus() != null ? request.getStatus() : 1);

            // 设置角色（默认ROLE_USER）
            Set<Role> roles = new HashSet<>();
            if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                for (String roleName : request.getRoles()) {
                    Role role = roleRepository.findByName(ERole.valueOf(roleName))
                            .orElseThrow(() -> new RuntimeException("Error: Role " + roleName + " not found."));
                    roles.add(role);
                }
            } else {
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role ROLE_USER not found."));
                roles.add(userRole);
            }
            user.setRoles(roles);

            User savedUser = userRepository.save(user);
            return new ResponseEntity<>(UserVO.from(savedUser), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new MessageResponse("Error: Failed to create user! " + e.getMessage()), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 更新用户
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long id, @Valid @RequestBody UserRequest request) {
        Optional<User> userData = userRepository.findById(id);

        if (userData.isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("Error: User not found!"), HttpStatus.NOT_FOUND);
        }

        try {
            User user = userData.get();

            // 更新邮箱（检查是否重复）
            if (!user.getEmail().equals(request.getEmail())) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    return ResponseEntity
                            .badRequest()
                            .body(new MessageResponse("Error: Email is already in use!"));
                }
                user.setEmail(request.getEmail());
            }

            // 更新密码（如果提供）
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            // 更新其他字段
            if (request.getRealName() != null) {
                user.setRealName(request.getRealName());
            }
            if (request.getPhone() != null) {
                user.setPhone(request.getPhone());
            }
            if (request.getUserType() != null) {
                user.setUserType(UserType.valueOf(request.getUserType()));
            }
            if (request.getStatus() != null) {
                user.setStatus(request.getStatus());
            }

            User updatedUser = userRepository.save(user);
            return new ResponseEntity<>(UserVO.from(updatedUser), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new MessageResponse("Error: Failed to update user! " + e.getMessage()), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 删除用户
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        try {
            if (!userRepository.existsById(id)) {
                return new ResponseEntity<>(new MessageResponse("Error: User not found!"), HttpStatus.NOT_FOUND);
            }

            userRepository.deleteById(id);
            return new ResponseEntity<>(new MessageResponse("User deleted successfully!"), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new MessageResponse("Error: Failed to delete user!"), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 分配角色给用户
    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignRoles(@PathVariable("userId") Long userId, 
                                         @RequestBody AssignRolesRequest request) {
        Optional<User> userData = userRepository.findById(userId);

        if (userData.isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("Error: User not found!"), HttpStatus.NOT_FOUND);
        }

        try {
            User user = userData.get();
            Set<Role> roles = new HashSet<>();

            // ✅ 改为 Integer
            for (Integer roleId : request.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new RuntimeException("Error: Role with ID " + roleId + " not found."));
                roles.add(role);
            }

            user.setRoles(roles);
            userRepository.save(user);

            return new ResponseEntity<>(new MessageResponse("Roles assigned successfully!"), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new MessageResponse("Error: Failed to assign roles! " + e.getMessage()), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 启用/禁用用户
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleUserStatus(@PathVariable("userId") Long userId, 
                                               @RequestBody StatusRequest request) {
        Optional<User> userData = userRepository.findById(userId);

        if (userData.isEmpty()) {
            return new ResponseEntity<>(new MessageResponse("Error: User not found!"), HttpStatus.NOT_FOUND);
        }

        try {
            User user = userData.get();
            user.setStatus(request.getStatus());
            userRepository.save(user);

            String statusText = request.getStatus() == 1 ? "enabled" : "disabled";
            return new ResponseEntity<>(new MessageResponse("User " + statusText + " successfully!"), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new MessageResponse("Error: Failed to update user status!"), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 用户VO（视图对象）
    @Data
    @AllArgsConstructor
    static class UserVO {
        private Long id;
        private String username;
        private String email;
        private String realName;
        private String phone;
        private String userType;
        private Integer status;
        private List<String> roles;

        static UserVO from(User u) {
            return new UserVO(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getRealName(),
                u.getPhone(),
                u.getUserType() != null ? u.getUserType().name() : null,
                u.getStatus(),
                u.getRoles().stream()
                    .map(Role::getName)
                    .map(Enum::name)
                    .collect(Collectors.toList())
            );
        }
    }

    // 请求DTO
    @Data
    static class UserRequest {
        private String username;
        private String email;
        private String password;
        private String realName;
        private String phone;
        private String userType;
        private Integer status;
        private List<String> roles;
    }

    @Data
    static class AssignRolesRequest {
        private List<Integer> roleIds;  // ✅ 改为 Integer
    }

    @Data
    static class StatusRequest {
        private Integer status;
    }
}