package com.petmanagement.petmanagementbackend.controllers;

import com.petmanagement.petmanagementbackend.models.ERole;
import com.petmanagement.petmanagementbackend.models.Role;
import com.petmanagement.petmanagementbackend.payload.response.MessageResponse;
import com.petmanagement.petmanagementbackend.repository.RoleRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/roles")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AdminRoleController {

    private final RoleRepository roleRepository;

    // 获取所有角色
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return ResponseEntity.ok(roles);
    }

    // 根据ID获取角色
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> getRoleById(@PathVariable("id") Integer id) {  // ✅ 改为 Integer
        Optional<Role> roleData = roleRepository.findById(id);
        return roleData.map(role -> new ResponseEntity<>(role, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 创建角色
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createRole(@Valid @RequestBody RoleRequest request) {
        try {
            // 检查角色名是否已存在
            ERole roleName = ERole.valueOf(request.getName());
            if (roleRepository.findByName(roleName).isPresent()) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Role name is already in use!"));
            }

            Role newRole = new Role();
            newRole.setName(roleName);
            Role savedRole = roleRepository.save(newRole);
            
            return new ResponseEntity<>(savedRole, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Invalid role name! Must be a valid ERole enum value."));
        } catch (Exception e) {
            return new ResponseEntity<>(new MessageResponse("Error: Failed to create role!"), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 更新角色
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRole(@PathVariable("id") Integer id, @Valid @RequestBody RoleRequest request) {  // ✅ 改为 Integer
        Optional<Role> roleData = roleRepository.findById(id);

        if (roleData.isPresent()) {
            try {
                Role role = roleData.get();
                ERole newRoleName = ERole.valueOf(request.getName());
                
                // 检查新名称是否与其他角色冲突
                Optional<Role> existingRole = roleRepository.findByName(newRoleName);
                if (existingRole.isPresent() && !existingRole.get().getId().equals(id)) {
                    return ResponseEntity
                            .badRequest()
                            .body(new MessageResponse("Error: Role name is already in use!"));
                }
                
                role.setName(newRoleName);
                return new ResponseEntity<>(roleRepository.save(role), HttpStatus.OK);
            } catch (IllegalArgumentException e) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Invalid role name!"));
            }
        } else {
            return new ResponseEntity<>(new MessageResponse("Error: Role not found!"), HttpStatus.NOT_FOUND);
        }
    }

    // 删除角色（系统角色不可删除）
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRole(@PathVariable("id") Integer id) {  // ✅ 改为 Integer
        try {
            Optional<Role> roleData = roleRepository.findById(id);
            if (roleData.isEmpty()) {
                return new ResponseEntity<>(new MessageResponse("Error: Role not found!"), HttpStatus.NOT_FOUND);
            }

            Role role = roleData.get();
            
            // 防止删除系统核心角色
            if (isSystemRole(role.getName())) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Cannot delete system role!"));
            }

            roleRepository.deleteById(id);  // ✅ 改为 Integer
            return new ResponseEntity<>(new MessageResponse("Role deleted successfully!"), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new MessageResponse("Error: Failed to delete role!"), 
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 检查是否为系统角色
    private boolean isSystemRole(ERole roleName) {
        return roleName == ERole.ROLE_USER || 
               roleName == ERole.ROLE_ADMIN || 
               roleName == ERole.ROLE_MERCHANT_HOSPITAL ||
               roleName == ERole.ROLE_MERCHANT_HOUSE ||
               roleName == ERole.ROLE_MERCHANT_GOODS;
    }

    // 内部请求DTO
    @lombok.Data
    static class RoleRequest {
        private String name;
    }
}