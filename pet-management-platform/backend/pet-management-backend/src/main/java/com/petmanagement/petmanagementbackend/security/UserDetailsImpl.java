package com.petmanagement.petmanagementbackend.security; // 包路径必须与文件夹结构一致

import com.petmanagement.petmanagementbackend.models.User; // 导入你的 User 实体类
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

// 实现 Spring Security 的 UserDetails 接口，封装用户认证信息
public class UserDetailsImpl implements UserDetails {
    private final User user;

    // 构造方法：传入 User 实体类对象
    public UserDetailsImpl(User user) {
        this.user = user;
    }

    // 获取用户ID（可选，用于业务逻辑中获取当前用户ID）
    public Long getId() {
        return user.getId(); // 确保你的 User 实体类有 `getId()` 方法（返回用户ID）
    }

    // 获取用户权限（无角色管理时返回空集合即可）
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // 无需角色时直接返回空集合
    }

    // 获取用户密码（需与 User 实体类的密码字段对应）
    @Override
    public String getPassword() {
        return user.getPassword(); // 确保 User 实体类有 `getPassword()` 方法
    }

    // 获取用户名（需与 User 实体类的用户名/邮箱字段对应）
    @Override
    public String getUsername() {
        return user.getUsername(); // 若用邮箱登录，改为 `user.getEmail()`（需确保 User 有该方法）
    }

    // 以下4个方法默认返回 true（表示账号正常，无过期/锁定）
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}