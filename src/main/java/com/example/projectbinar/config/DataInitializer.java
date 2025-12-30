package com.example.projectbinar.config;

import com.example.projectbinar.entity.Permission;
import com.example.projectbinar.entity.Plafond;
import com.example.projectbinar.entity.Role;
import com.example.projectbinar.entity.User;
import com.example.projectbinar.repository.PermissionRepository;
import com.example.projectbinar.repository.PlafondRepository;
import com.example.projectbinar.repository.RoleRepository;
import com.example.projectbinar.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final PlafondRepository plafondRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository,
                           PermissionRepository permissionRepository, PlafondRepository plafondRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.permissionRepository = permissionRepository;
        this.plafondRepository = plafondRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        initializePermissions();
        initializeRoles();
        initializePlafonds();
        initializeDefaultAdmin();
    }

    private void initializePermissions() {
        String[][] permissions = {
                {"LOAN_CREATE", "Create loan application"},
                {"LOAN_VIEW", "View loan applications"},
                {"LOAN_REVIEW", "Review loan applications (Marketing)"},
                {"LOAN_APPROVE", "Approve/Reject loan applications (Branch Manager)"},
                {"LOAN_DISBURSE", "Process loan disbursement (Back Office)"},
                {"USER_MANAGE", "Manage users"},
                {"ROLE_MANAGE", "Manage roles and permissions"},
                {"PLAFOND_MANAGE", "Manage plafond/products"}
        };

        for (String[] perm : permissions) {
            if (!permissionRepository.existsByName(perm[0])) {
                Permission permission = Permission.builder()
                        .name(perm[0])
                        .description(perm[1])
                        .build();
                permissionRepository.save(permission);
                logger.info("Created permission: {}", perm[0]);
            }
        }
    }

    private void initializeRoles() {
        // Define roles with their permissions
        Map<String, String[]> rolePermissions = new HashMap<>();
        rolePermissions.put("SUPER_ADMIN", new String[]{
                "LOAN_CREATE", "LOAN_VIEW", "LOAN_REVIEW", "LOAN_APPROVE", "LOAN_DISBURSE",
                "USER_MANAGE", "ROLE_MANAGE", "PLAFOND_MANAGE"
        });
        rolePermissions.put("MARKETING", new String[]{"LOAN_VIEW", "LOAN_REVIEW"});
        rolePermissions.put("BRANCH_MANAGER", new String[]{"LOAN_VIEW", "LOAN_APPROVE"});
        rolePermissions.put("BACK_OFFICE", new String[]{"LOAN_VIEW", "LOAN_DISBURSE"});
        rolePermissions.put("USER", new String[]{"LOAN_CREATE", "LOAN_VIEW"});

        for (Map.Entry<String, String[]> entry : rolePermissions.entrySet()) {
            String roleName = entry.getKey();
            String[] permNames = entry.getValue();

            Role role = roleRepository.findByName(roleName).orElse(null);
            if (role == null) {
                role = Role.builder()
                        .name(roleName)
                        .description("Role: " + roleName)
                        .permissions(new HashSet<>())
                        .build();
            }

            // Add permissions to role
            for (String permName : permNames) {
                Permission perm = permissionRepository.findByName(permName).orElse(null);
                if (perm != null) {
                    role.getPermissions().add(perm);
                }
            }


            roleRepository.save(role);
            logger.info("Created/Updated role: {} with {} permissions", roleName, role.getPermissions().size());
        }
    }

    private void initializePlafonds() {
        if (plafondRepository.count() == 0) {
            Plafond silver = Plafond.builder()
                    .name("Silver")
                    .minAmount(new BigDecimal("1000000"))
                    .maxAmount(new BigDecimal("5000000"))
                    .interestRate(new BigDecimal("12.00"))
                    .tenorMonth(6)
                    .isActive(true)
                    .build();
            plafondRepository.save(silver);

            Plafond gold = Plafond.builder()
                    .name("Gold")
                    .minAmount(new BigDecimal("5000000"))
                    .maxAmount(new BigDecimal("20000000"))
                    .interestRate(new BigDecimal("10.00"))
                    .tenorMonth(12)
                    .isActive(true)
                    .build();
            plafondRepository.save(gold);

            Plafond platinum = Plafond.builder()
                    .name("Platinum")
                    .minAmount(new BigDecimal("20000000"))
                    .maxAmount(new BigDecimal("100000000"))
                    .interestRate(new BigDecimal("8.00"))
                    .tenorMonth(24)
                    .isActive(true)
                    .build();
            plafondRepository.save(platinum);

            logger.info("Created plafonds: Silver, Gold, Platinum");
        }
    }

    private void initializeDefaultAdmin() {
        if (userRepository.findByUsername("superadmin").isEmpty()) {
            Role superAdminRole = roleRepository.findByName("SUPER_ADMIN")
                    .orElseThrow(() -> new RuntimeException("SUPER_ADMIN role not found"));

            Set<Role> roles = new HashSet<>();
            roles.add(superAdminRole);

            User admin = User.builder()
                    .username("superadmin")
                    .email("superadmin@loanapp.com")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .fullname("Super Administrator")
                    .phone("08123456789")
                    .createdAt(Instant.now())
                    .isActive(true)
                    .roles(roles)
                    .build();
            userRepository.save(admin);
            logger.info("Created default superadmin user (username: superadmin, password: Admin@123)");
        }
    }
}
