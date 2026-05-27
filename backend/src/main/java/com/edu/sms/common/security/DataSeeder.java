package com.edu.sms.common.security;

import com.edu.sms.modules.auth.model.AuthUser;
import com.edu.sms.modules.auth.model.Role;
import com.edu.sms.modules.auth.repository.AuthUserRepository;
import com.edu.sms.modules.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedRoles();
        seedAdminUser();
    }

    private void seedRoles() {
        String[] roles = {"ROLE_SUPER_ADMIN", "ROLE_ADMIN", "ROLE_TEACHER", "ROLE_STUDENT"};
        for (String roleName : roles) {
            if (!roleRepository.existsByName(roleName)) {
                roleRepository.save(Role.builder().name(roleName).description(roleName).build());
                log.info("[Seeder] Created role: {}", roleName);
            }
        }
    }

    private void seedAdminUser() {
        String adminEmail = "admin@sms.edu";
        if (!authUserRepository.existsByEmail(adminEmail)) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new IllegalStateException("ROLE_ADMIN chưa được khởi tạo"));

            AuthUser admin = AuthUser.builder()
                    .username("admin")
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .roles(Set.of(adminRole))
                    .isActive(true)
                    .build();

            authUserRepository.save(admin);
            log.info("[Seeder] Created default admin: {} / Admin@123", adminEmail);
        }
    }
}
