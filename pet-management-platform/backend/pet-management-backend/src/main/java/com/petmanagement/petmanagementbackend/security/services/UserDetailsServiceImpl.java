package com.petmanagement.petmanagementbackend.security.services;

import com.petmanagement.petmanagementbackend.models.User;
import com.petmanagement.petmanagementbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        System.out.println("==============================================");
        System.out.println("【登录时】从数据库取出的 admin 密码哈希: " + user.getPassword());
        System.out.println("==============================================");

        return UserDetailsImpl.build(user);
    }
}