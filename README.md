# Spring Boot - SQL Server Project

## Jelaskan fungsi utama setiap anotasi JPA
- **@Entity**: Menandakan bahwa class ini adalah object yang akan dipetakan ke table database.
- **@Table**: Menentukan nama table di database yang berkorespondensi dengan entity ini.
- **@Id**: Menandakan field ini adalah Primary Key.
- **@GeneratedValue**: Mengatur strategi pembuatan ID otomatis (misal IDENTITY).
- **@Column**: Mengatur detail kolom seperti nama kolom di database, unique, nullable, dll.
- **@ManyToMany**: Menandakan relasi many-to-many, dimana satu User bisa punya banyak Role, dan satu Role bisa dimiliki banyak User.
- **@JoinTable**: Mengatur table perantara (join table) untuk relasi Many-to-Many.
    - `name`: Nama table perantara (`user_roles`).
    - `joinColumns`: Foreign key dari entity saat ini (`user_id`).
    - `inverseJoinColumns`: Foreign key dari entity lawan (`role_id`).

## Kenapa terbentuk 3 table (users, roles, user_roles)?
Ini karena kita menggunakan relasi **Many-to-Many**.
1. **Table `users`**: Menyimpan data user (username, password, email).
2. **Table `roles`**: Menyimpan data role (ADMIN, USER).
3. **Table `user_roles`**: Table perantara yang mencatat hubungan antara user dan role. Karena satu user bisa punya banyak role dan role bisa dipakai banyak user, kita tidak bisa menyimpan FK di salah satu table utama saja. Kita butuh table ketiga yang berisi pasangan (`user_id`, `role_id`).

## Alur ORM dari request sampai database
1. **Request**: Client mengirim JSON via HTTP (misal POST /users).
2. **Controller**: Menerima request, mem-parsing body JSON ke Object Java (`User`), dan memanggil Service.
3. **Service**: Melakukan business logic (jika ada), lalu memanggil Repository.
4. **Repository (Spring Data JPA)**:
    - Method `save()` dipanggil.
    - Hibernate (sebagai implementasi JPA) membaca entity dan statusnya.
    - Hibernate membuat SQL statement (`INSERT INTO users ...`).
    - Jika ada relasi (roles), Hibernate juga menginsert ke table `user_roles`.
5. **JDBC Driver (SQL Server)**: Mengirimkan perintah SQL tersebut ke Database SQL Server.
6. **Database**: Mengeksekusi SQL dan menyimpan data fisik.

## Cara Menjalankan
1. Pastikan SQL Server database `projectbinar` sudah dibuat atau user memiliki hak create database.
2. Setup username & password database di `src/main/resources/application.yml`.
3. Run `ProjectBinarApplication`.
