package com.petmanagement.petmanagementbackend.controllers;

import com.petmanagement.petmanagementbackend.models.Role;
import com.petmanagement.petmanagementbackend.models.User;
import com.petmanagement.petmanagementbackend.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserVO>> getAllUsers() {
        List<UserVO> list = userRepository.findAll().stream().map(UserVO::from).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @Data @AllArgsConstructor
    static class UserVO {
        private Long id;
        private String username;
        private String email;
        private String userType;
        private Integer status;
        private List<String> roles;

        static UserVO from(User u) {
            return new UserVO(
                u.getId(), u.getUsername(), u.getEmail(),
                u.getUserType() != null ? u.getUserType().name() : null,
                u.getStatus(),
                u.getRoles().stream().map(Role::getName).map(Enum::name).collect(Collectors.toList())
            );
        }
    }
}