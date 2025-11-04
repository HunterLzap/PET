package com.petmanagement.petmanagementbackend.config;

import com.petmanagement.petmanagementbackend.models.ERole;
import com.petmanagement.petmanagementbackend.models.Role;
import com.petmanagement.petmanagementbackend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataLoader(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        /*// Check if roles exist, if not, create them
        if (roleRepository.findByName(ERole.ROLE_PET_OWNER).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_PET_OWNER));
        }
        if (roleRepository.findByName(ERole.ROLE_BUSINESS).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_BUSINESS));
        }
        if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_ADMIN));*/
        }
}


