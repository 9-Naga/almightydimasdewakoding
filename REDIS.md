# Redis Caching Implementation

Dokumentasi implementasi Redis sebagai caching layer di project Spring Boot ini.

## Arsitektur Caching

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Client    │───>│ Controller  │───>│   Service   │───>│  Database   │
└─────────────┘    └─────────────┘    └──────┬──────┘    └─────────────┘
                                             │
                                             ▼
                                      ┌─────────────┐
                                      │    Redis    │
                                      │   Cache     │
                                      └─────────────┘
```

## Prerequisites

Redis harus sudah berjalan sebelum menjalankan aplikasi.

### Menjalankan Redis dengan Docker

```bash
# Start Redis container
docker-compose up -d

# Verifikasi Redis running
docker ps

# Connect ke Redis CLI
docker exec -it redis_container redis-cli
```

## Konfigurasi

### application.yml

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 60000
  cache:
    type: redis
    redis:
      time-to-live: 600000  # 10 menit
```

### RedisConfig.java

Konfigurasi meliputi:
- **RedisTemplate**: Template untuk operasi Redis dengan Jackson JSON serializer
- **CacheManager**: Manager untuk mengatur cache dengan TTL 10 menit

## Penggunaan Caching Annotations

### @Cacheable
Data disimpan ke cache setelah pertama kali diambil:

```java
@Cacheable(value = "users")
public List<User> getAllUsers() {
    return userRepository.findAll();
}

@Cacheable(value = "user", key = "#id")
public Optional<User> getUserById(Long id) {
    return userRepository.findById(id);
}
```

### @CacheEvict
Cache dihapus saat data berubah:

```java
@CacheEvict(value = {"users", "user"}, allEntries = true)
public void deleteUser(Long id) {
    userRepository.deleteById(id);
}
```

## Cache Keys

| Cache Name | Deskripsi | Service |
|------------|-----------|---------|
| `users` | List semua users | UserService |
| `user` | Single user by ID | UserService |
| `roles` | List semua roles | RoleService |
| `role` | Single role by ID | RoleService |

## Testing & Verifikasi

### 1. Cek Cache di Redis CLI

```bash
docker exec -it redis_container redis-cli

# Lihat semua keys
KEYS *

# Lihat isi key tertentu
GET "users::SimpleKey []"

# Hapus semua cache
FLUSHALL
```

### 2. Verifikasi via Log

Saat `show-sql: true`, perhatikan:
- **Request pertama**: Ada SQL statement di log
- **Request kedua (cached)**: TIDAK ada SQL statement

### 3. Testing Manual

```bash
# Request pertama - query database
curl http://localhost:8080/api/users

# Request kedua - dari cache (lebih cepat, tidak ada SQL log)
curl http://localhost:8080/api/users
```

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Redis connection refused | Pastikan container running: `docker ps` |
| Cache tidak bekerja | Cek `@EnableCaching` di main class |
| Serialization error | Pastikan entity implement `Serializable` |

## File yang Dimodifikasi

- `pom.xml` - Dependency redis
- `application.yml` - Konfigurasi redis
- `RedisConfig.java` - Redis configuration [NEW]
- `User.java` - Implements Serializable
- `Role.java` - Implements Serializable
- `UserService.java` - Caching annotations
- `RoleService.java` - Caching annotations
- `ProjectbinarApplication.java` - @EnableCaching
