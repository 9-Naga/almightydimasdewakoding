package com.example.projectbinar.config;

import com.example.projectbinar.entity.CustomerProfile;
import com.example.projectbinar.entity.LoanApplication;
import com.example.projectbinar.entity.Permission;
import com.example.projectbinar.entity.Plafond;
import com.example.projectbinar.entity.Role;
import com.example.projectbinar.entity.User;
import com.example.projectbinar.enums.LoanStatus;
import com.example.projectbinar.repository.CustomerProfileRepository;
import com.example.projectbinar.repository.LoanApplicationRepository;
import com.example.projectbinar.repository.PermissionRepository;
import com.example.projectbinar.repository.PlafondRepository;
import com.example.projectbinar.repository.RoleRepository;
import com.example.projectbinar.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PermissionRepository permissionRepository;
  private final PlafondRepository plafondRepository;
  private final CustomerProfileRepository customerProfileRepository;
  private final LoanApplicationRepository loanApplicationRepository;
  private final PasswordEncoder passwordEncoder;

  public DataInitializer(
      RoleRepository roleRepository,
      UserRepository userRepository,
      PermissionRepository permissionRepository,
      PlafondRepository plafondRepository,
      CustomerProfileRepository customerProfileRepository,
      LoanApplicationRepository loanApplicationRepository,
      PasswordEncoder passwordEncoder) {
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
    this.permissionRepository = permissionRepository;
    this.plafondRepository = plafondRepository;
    this.customerProfileRepository = customerProfileRepository;
    this.loanApplicationRepository = loanApplicationRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) throws Exception {
    initializePermissions();
    initializeRoles();
    initializePlafonds();
    initializeDefaultAdmin();
    initializeMarketingUser();
    initializeBranchManagerUser();
    initializeCustomerUser();
    initializeBackOfficeUser();

    // Initialize 5 dummy customers with loan applications
    initializeDummyCustomersWithLoans();
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
        Permission permission = Permission.builder().name(perm[0]).description(perm[1]).build();
        permissionRepository.save(permission);
        logger.info("Created permission: {}", perm[0]);
      }
    }
  }

  private void initializeRoles() {
    // Define roles with their permissions
    Map<String, String[]> rolePermissions = new HashMap<>();
    rolePermissions.put(
        "SUPER_ADMIN",
        new String[] {
          "LOAN_CREATE",
          "LOAN_VIEW",
          "LOAN_REVIEW",
          "LOAN_APPROVE",
          "LOAN_DISBURSE",
          "USER_MANAGE",
          "ROLE_MANAGE",
          "PLAFOND_MANAGE"
        });
    rolePermissions.put("MARKETING", new String[] {"LOAN_VIEW", "LOAN_REVIEW"});
    rolePermissions.put("BRANCH_MANAGER", new String[] {"LOAN_VIEW", "LOAN_APPROVE"});
    rolePermissions.put("BACK_OFFICE", new String[] {"LOAN_VIEW", "LOAN_DISBURSE"});
    rolePermissions.put("USER", new String[] {"LOAN_CREATE", "LOAN_VIEW"});

    for (Map.Entry<String, String[]> entry : rolePermissions.entrySet()) {
      String roleName = entry.getKey();
      String[] permNames = entry.getValue();

      Role role = roleRepository.findByName(roleName).orElse(null);
      if (role == null) {
        role =
            Role.builder()
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
      logger.info(
          "Created/Updated role: {} with {} permissions", roleName, role.getPermissions().size());
    }
  }

  private void initializePlafonds() {
    if (plafondRepository.count() == 0) {
      // Bronze: 1,000,000 - 4,999,999 (1-5 juta)
      // Interest rate: 15% (highest, untuk amount terkecil)
      Plafond bronze =
          Plafond.builder()
              .name("Bronze")
              .minAmount(new BigDecimal("1000000"))
              .maxAmount(new BigDecimal("4999999"))
              .interestRate(new BigDecimal("15.00"))
              .tenorMonth(12)
              .isActive(true)
              .build();
      plafondRepository.save(bronze);

      // Silver: 5,000,000 - 19,999,999 (5-20 juta)
      // Interest rate: 12%
      Plafond silver =
          Plafond.builder()
              .name("Silver")
              .minAmount(new BigDecimal("5000000"))
              .maxAmount(new BigDecimal("19999999"))
              .interestRate(new BigDecimal("12.00"))
              .tenorMonth(24)
              .isActive(true)
              .build();
      plafondRepository.save(silver);

      // Gold: 20,000,000 - 49,999,999 (20-50 juta)
      // Interest rate: 10%
      Plafond gold =
          Plafond.builder()
              .name("Gold")
              .minAmount(new BigDecimal("20000000"))
              .maxAmount(new BigDecimal("49999999"))
              .interestRate(new BigDecimal("10.00"))
              .tenorMonth(36)
              .isActive(true)
              .build();
      plafondRepository.save(gold);

      // Platinum: 50,000,000 - 99,999,999 (50-100 juta)
      // Interest rate: 8%
      Plafond platinum =
          Plafond.builder()
              .name("Platinum")
              .minAmount(new BigDecimal("50000000"))
              .maxAmount(new BigDecimal("99999999"))
              .interestRate(new BigDecimal("8.00"))
              .tenorMonth(48)
              .isActive(true)
              .build();
      plafondRepository.save(platinum);

      // Diamond: 100,000,000 - 500,000,000 (100-500 juta)
      // Interest rate: 6% (lowest, untuk premium customers)
      Plafond diamond =
          Plafond.builder()
              .name("Diamond")
              .minAmount(new BigDecimal("100000000"))
              .maxAmount(new BigDecimal("500000000"))
              .interestRate(new BigDecimal("6.00"))
              .tenorMonth(60)
              .isActive(true)
              .build();
      plafondRepository.save(diamond);

      logger.info(
          "Created 5 plafond products: Bronze (1-5M/12mo/15%), Silver (5-20M/24mo/12%), "
              + "Gold (20-50M/36mo/10%), Platinum (50-100M/48mo/8%), Diamond (100-500M/60mo/6%)");
    }
  }

  private void initializeDefaultAdmin() {
    if (userRepository.findByUsername("superadmin").isEmpty()) {
      Role superAdminRole =
          roleRepository
              .findByName("SUPER_ADMIN")
              .orElseThrow(() -> new RuntimeException("SUPER_ADMIN role not found"));

      Set<Role> roles = new HashSet<>();
      roles.add(superAdminRole);

      User admin =
          User.builder()
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

  private void initializeMarketingUser() {
    createOrUpdateUser("marketing01", "mkt01@test.com", "Marketing Staff", "MARKETING");
  }

  private void initializeBranchManagerUser() {
    createOrUpdateUser("branch01", "branch01@test.com", "Branch Manager", "BRANCH_MANAGER");
  }

  private void initializeCustomerUser() {
    createOrUpdateUser("customer01", "customer01@test.com", "Nasabah Test", "USER");
  }

  private void initializeBackOfficeUser() {
    createOrUpdateUser("backoffice01", "backoffice01@test.com", "Back Office Staff", "BACK_OFFICE");
  }

  /**
   * Initialize 5 dummy customers with complete profiles and loan applications. Each customer has
   * different loan amounts and tenors to demonstrate the dynamic system.
   */
  private void initializeDummyCustomersWithLoans() {
    // Dummy customer data: username, email, fullName, NIK, address, bankName, accountNo, amount,
    // tenor
    Object[][] dummyData = {
      // Customer 1: Bronze product - Rp 3,000,000, 6 months
      {
        "budi_santoso",
        "budi.santoso@email.com",
        "Budi Santoso",
        "3201011234567890",
        "Jl. Merdeka No. 10, Jakarta Pusat",
        "BCA",
        "1234567890",
        new BigDecimal("3000000"),
        6
      },
      // Customer 2: Silver product - Rp 12,000,000, 18 months
      {
        "siti_rahayu",
        "siti.rahayu@email.com",
        "Siti Rahayu",
        "3201012345678901",
        "Jl. Sudirman No. 25, Jakarta Selatan",
        "Mandiri",
        "0987654321",
        new BigDecimal("12000000"),
        18
      },
      // Customer 3: Gold product - Rp 30,000,000, 24 months
      {
        "ahmad_wijaya",
        "ahmad.wijaya@email.com",
        "Ahmad Wijaya",
        "3201013456789012",
        "Jl. Gatot Subroto No. 100, Jakarta Barat",
        "BNI",
        "1122334455",
        new BigDecimal("30000000"),
        24
      },
      // Customer 4: Platinum product - Rp 75,000,000, 36 months
      {
        "dewi_lestari",
        "dewi.lestari@email.com",
        "Dewi Lestari",
        "3201014567890123",
        "Jl. Thamrin No. 50, Jakarta Pusat",
        "BRI",
        "5566778899",
        new BigDecimal("75000000"),
        36
      },
      // Customer 5: Diamond product - Rp 200,000,000, 48 months
      {
        "rudi_hermawan",
        "rudi.hermawan@email.com",
        "Rudi Hermawan",
        "3201015678901234",
        "Jl. Kuningan No. 75, Jakarta Selatan",
        "CIMB Niaga",
        "9988776655",
        new BigDecimal("200000000"),
        48
      }
    };

    Role userRole =
        roleRepository
            .findByName("USER")
            .orElseThrow(() -> new RuntimeException("USER role not found"));

    for (Object[] data : dummyData) {
      String username = (String) data[0];
      String email = (String) data[1];
      String fullName = (String) data[2];
      String nik = (String) data[3];
      String address = (String) data[4];
      String bankName = (String) data[5];
      String accountNo = (String) data[6];
      BigDecimal amount = (BigDecimal) data[7];
      Integer tenorMonth = (Integer) data[8];

      // Create user if not exists
      User user =
          userRepository
              .findByUsername(username)
              .orElseGet(
                  () -> {
                    Set<Role> roles = new HashSet<>();
                    roles.add(userRole);
                    User newUser =
                        User.builder()
                            .username(username)
                            .email(email)
                            .passwordHash(passwordEncoder.encode("password123"))
                            .fullname(fullName)
                            .phone("081234567890")
                            .createdAt(Instant.now())
                            .isActive(true)
                            .roles(roles)
                            .build();
                    return userRepository.save(newUser);
                  });

      // Create customer profile if not exists
      CustomerProfile profile =
          customerProfileRepository
              .findByUser(user)
              .orElseGet(
                  () -> {
                    CustomerProfile newProfile =
                        CustomerProfile.builder()
                            .user(user)
                            .fullName(fullName)
                            .identityNumber(nik)
                            .address(address)
                            .bankName(bankName)
                            .bankAccountNumber(accountNo)
                            .bankAccountHolderName(fullName)
                            .uploadKtp("dummy_ktp_base64_data")
                            .createdAt(Instant.now())
                            .build();
                    return customerProfileRepository.save(newProfile);
                  });

      // Check if this customer already has ANY loan application
      // If so, skip creating a new one to verify idempotency
      if (!loanApplicationRepository.findByCustomer(profile).isEmpty()) {
        logger.info("Loan application already exists for user: {}, skipping.", username);
        continue;
      }

      // Find appropriate plafond based on amount
      Optional<Plafond> plafondOpt = plafondRepository.findActiveByAmount(amount);
      if (plafondOpt.isEmpty()) {
        logger.warn("No plafond found for amount: {}", amount);
        continue;
      }

      Plafond plafond = plafondOpt.get();

      // Calculate dynamic interest rate
      BigDecimal actualInterestRate =
          calculateDynamicInterestRate(
              plafond.getInterestRate(), tenorMonth, plafond.getTenorMonth());

      // Calculate loan details
      BigDecimal totalInterest = calculateTotalInterest(amount, actualInterestRate, tenorMonth);
      BigDecimal totalPayment = amount.add(totalInterest);
      BigDecimal monthlyInstallment =
          totalPayment.divide(new BigDecimal(tenorMonth), 2, RoundingMode.HALF_UP);

      // Create loan application
      LoanApplication loan =
          LoanApplication.builder()
              .customer(profile)
              .plafond(plafond)
              .amount(amount)
              .tenorMonth(tenorMonth)
              .interestRate(actualInterestRate)
              .totalInterest(totalInterest)
              .totalPayment(totalPayment)
              .monthlyInstallment(monthlyInstallment)
              .status(LoanStatus.SUBMITTED)
              .build();

      loanApplicationRepository.save(loan);

      logger.info(
          "Created dummy customer: {} with loan Rp{} ({} months) - Product: {}, Rate: {}%",
          username,
          String.format("%,.0f", amount),
          tenorMonth,
          plafond.getName(),
          actualInterestRate);
    }

    logger.info("✅ Verified/Created 5 dummy customers with loan applications for testing");
  }

  private BigDecimal calculateDynamicInterestRate(
      BigDecimal baseRate, Integer selectedTenor, Integer maxTenor) {
    BigDecimal tenorRatio =
        new BigDecimal(selectedTenor).divide(new BigDecimal(maxTenor), 4, RoundingMode.HALF_UP);

    // Minimum floor of 50%
    BigDecimal minRatio = new BigDecimal("0.5");
    if (tenorRatio.compareTo(minRatio) < 0) {
      tenorRatio = minRatio;
    }

    return baseRate.multiply(tenorRatio).setScale(2, RoundingMode.HALF_UP);
  }

  private BigDecimal calculateTotalInterest(
      BigDecimal principal, BigDecimal interestRate, Integer tenorMonth) {
    return principal
        .multiply(interestRate)
        .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
        .multiply(new BigDecimal(tenorMonth))
        .divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
  }

  private void createOrUpdateUser(String username, String email, String fullname, String roleName) {
    Role role =
        roleRepository
            .findByName(roleName)
            .orElseThrow(() -> new RuntimeException("Role " + roleName + " not found"));

    userRepository
        .findByUsername(username)
        .ifPresentOrElse(
            user -> {
              // Update role if exists but wrong
              boolean hasRole =
                  user.getRoles().stream().anyMatch(r -> r.getName().equals(roleName));
              if (!hasRole) {
                user.getRoles().clear();
                user.getRoles().add(role);
                userRepository.save(user);
                logger.info("Updated role for user: {} to {}", username, roleName);
              }
            },
            () -> {
              // Create new
              Set<Role> roles = new HashSet<>();
              roles.add(role);
              User user =
                  User.builder()
                      .username(username)
                      .email(email)
                      .passwordHash(passwordEncoder.encode("password123"))
                      .fullname(fullname)
                      .phone("08123456789")
                      .createdAt(Instant.now())
                      .isActive(true)
                      .roles(roles)
                      .build();
              userRepository.save(user);
              logger.info("Created user: {} with role: {}", username, roleName);
            });
  }
}
