package com.example.projectbinar.config;

import com.example.projectbinar.entity.Role;
import com.example.projectbinar.entity.User;
import com.example.projectbinar.repository.RoleRepository;
import com.example.projectbinar.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (roleRepository.count() == 0) {
            Role adminRole = Role.builder().name("ADMIN").build();
            Role userRole = Role.builder().name("USER").build();
            roleRepository.save(adminRole);
            roleRepository.save(userRole);

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(userRole);

            User admin = User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .password("password")
                    .isActive(true)
                    .roles(roles)
                    .build();
            userRepository.save(admin);
        }
    }
}
