# Loan Management System - Backend API

Aplikasi backend untuk sistem pinjaman online (Loan Management System) berbasis Spring Boot.

## Tech Stack

- **Framework**: Spring Boot 3.3.0
- **Database**: SQL Server
- **ORM**: Spring Data JPA (Hibernate)
- **Security**: Spring Security + JWT (HMAC256)
- **Cache/Token Storage**: Redis
- **API Documentation**: Swagger/OpenAPI
- **Build Tool**: Maven
- **Java Version**: 21

## Fitur

### Authentication & Authorization
- ✅ Register user baru
- ✅ Login dengan JWT token
- ✅ Logout (token invalidation via Redis)
- ✅ Forgot Password dengan UUID token
- ✅ Reset Password
- ✅ Dynamic RBAC berbasis Permission

### Role & Permission
| Role | Permissions |
|------|-------------|
| SUPER_ADMIN | All permissions |
| MARKETING | LOAN_VIEW, LOAN_REVIEW |
| BRANCH_MANAGER | LOAN_VIEW, LOAN_APPROVE |
| BACK_OFFICE | LOAN_VIEW, LOAN_DISBURSE |
| USER | LOAN_CREATE, LOAN_VIEW |

### Loan Application Flow
1. **USER** → Register → Complete Profile (+ upload KTP) → Submit Loan Application
2. **MARKETING** → Review application → Approve/Reject
3. **BRANCH_MANAGER** → Approve/Reject reviewed applications
4. **BACK_OFFICE** → Process disbursement for approved loans
5. **USER** → Receive notifications at each step

### Products/Plafond
- **Silver**: Rp 1.000.000 - 5.000.000, 12% interest, 6 months tenor
- **Gold**: Rp 5.000.000 - 20.000.000, 10% interest, 12 months tenor
- **Platinum**: Rp 20.000.000 - 100.000.000, 8% interest, 24 months tenor

## Prerequisites

1. **Java 21** - JDK 21 or higher
2. **Maven** - For building the project
3. **SQL Server** - Database server running on localhost:1433
4. **Redis** - Running on localhost:6379

## Database Setup

1. Create a database named `Loan_db` in SQL Server
2. Create a user with credentials:
   - Username: `app_user`
   - Password: `Password123!`
3. Grant the user permissions to the database

## Running the Application

### 1. Start Redis (using Docker)
```bash
docker run -d --name redis -p 6379:6379 redis
```

Or use docker-compose:
```bash
docker-compose up -d
```

### 2. Build the project
```bash
mvn clean compile
```

### 3. Run the application
```bash
mvn spring-boot:run
```

The application will start on **http://localhost:8081**

## API Documentation

Swagger UI is available at:
- **URL**: http://localhost:8081/swagger-ui.html

## API Endpoints

### Public Endpoints (No Auth Required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login and get JWT |
| POST | `/api/auth/forgot-password` | Request password reset token |
| POST | `/api/auth/reset-password` | Reset password with token |
| GET | `/api/plafonds` | Get all active products |
| GET | `/api/plafonds/{id}` | Get product details |

### User Endpoints (Requires USER role)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/profile` | Get own profile |
| PUT | `/api/profile` | Update profile |
| POST | `/api/loans` | Submit loan application |
| GET | `/api/loans` | Get own loans |
| GET | `/api/notifications` | Get notifications |

### Marketing Endpoints (Requires MARKETING role)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/reviews/pending` | Get pending reviews |
| POST | `/api/reviews/{loanId}` | Submit review |

### Branch Manager Endpoints (Requires BRANCH_MANAGER role)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/approvals/pending` | Get pending approvals |
| POST | `/api/approvals/{loanId}` | Submit approval |

### Back Office Endpoints (Requires BACK_OFFICE role)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/disbursements/pending` | Get pending disbursements |
| POST | `/api/disbursements/{loanId}` | Process disbursement |

### Admin Endpoints (Requires SUPER_ADMIN role)
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/roles` | Get all roles |
| POST | `/api/admin/roles` | Create role |
| GET | `/api/admin/permissions` | Get all permissions |
| POST | `/api/admin/roles/{roleId}/permissions` | Assign permission |

## Default Admin Account

After first run, the following admin account is created:
- **Username**: `superadmin`
- **Password**: `Admin@123`

## Testing with Postman

### 1. Login
```json
POST /api/auth/login
{
    "username": "superadmin",
    "password": "Admin@123"
}
```

### 2. Use the Token
Add header to all authenticated requests:
```
Authorization: Bearer <your_jwt_token>
```

### 3. Register a New User
```json
POST /api/auth/register
{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123",
    "fullname": "John Doe",
    "phone": "08123456789"
}
```

### 4. Complete Profile (as USER)
```json
PUT /api/profile
{
    "fullName": "John Doe",
    "address": "Jl. Example No. 123",
    "identityNumber": "1234567890123456",
    "bankName": "BCA",
    "bankAccountNumber": "1234567890",
    "bankAccountHolderName": "John Doe",
    "uploadKtp": "data:image/jpeg;base64,/9j/4AAQSkZJRg..."
}
```

### 5. Submit Loan Application (as USER)
```json
POST /api/loans
{
    "plafondId": 1,
    "amount": 3000000
}
```

## Project Structure

```
src/main/java/com/example/projectbinar/
├── ProjectbinarApplication.java
├── base/
│   └── ApiResponse.java
├── config/
│   ├── DataInitializer.java
│   ├── OpenApiConfig.java
│   ├── RedisConfig.java
│   └── SecurityConfig.java
├── controller/
│   ├── ApprovalController.java
│   ├── AuthController.java
│   ├── CustomerProfileController.java
│   ├── DisbursementController.java
│   ├── LoanApplicationController.java
│   ├── NotificationController.java
│   ├── PlafondController.java
│   ├── ReviewController.java
│   └── RolePermissionController.java
├── dto/
│   ├── admin/
│   ├── auth/
│   ├── loan/
│   ├── notification/
│   ├── plafond/
│   ├── profile/
│   └── user/
├── entity/
│   ├── CustomerProfile.java
│   ├── Disbursement.java
│   ├── LoanApplication.java
│   ├── LoanApproval.java
│   ├── LoanReview.java
│   ├── Notification.java
│   ├── PasswordResetToken.java
│   ├── Permission.java
│   ├── Plafond.java
│   ├── Role.java
│   └── User.java
├── enums/
│   ├── ApprovalStatus.java
│   ├── DisbursementStatus.java
│   ├── LoanStatus.java
│   ├── NotificationChannel.java
│   ├── NotificationType.java
│   └── ReviewStatus.java
├── exception/
│   ├── BadRequestException.java
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── UnauthorizedException.java
├── repository/
│   └── (all repositories)
├── security/
│   ├── CustomUserDetails.java
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtUtils.java
│   ├── PermissionEvaluator.java
│   └── RedisTokenService.java
└── service/
    ├── AuthService.java
    ├── CustomerProfileService.java
    ├── DisbursementService.java
    ├── LoanApplicationService.java
    ├── LoanApprovalService.java
    ├── LoanReviewService.java
    ├── NotificationService.java
    ├── PlafondService.java
    └── RolePermissionService.java
```

## Troubleshooting

### Issue: Redis Connection Error
Make sure Redis is running on localhost:6379:
```bash
docker ps | grep redis
```

### Issue: Database Connection Error
1. Verify SQL Server is running
2. Check database credentials in `application.yml`
3. Ensure `Loan_db` database exists

### Issue: JWT Token Invalid
- Tokens expire after 24 hours
- Ensure you're using the correct format: `Bearer <token>`
- Check if token was blacklisted (logout)

## License

MIT License
