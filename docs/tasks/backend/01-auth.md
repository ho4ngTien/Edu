# Backend Checklist: Module 01 - Authentication (Auth)

**Trạng thái:** ✅ Done
**Tiến độ:** 100%

## Cấu hình Spring Security
- [x] Thêm dependencies: `spring-boot-starter-security`, `jjwt-api`, `jjwt-impl`, `jjwt-jackson`.
- [x] Khởi tạo class `SecurityConfig` tại `common/security/SecurityConfig.java`.
- [x] Cấu hình `SecurityFilterChain`: Tắt CSRF, thiết lập chế độ Stateless (không dùng Session).
- [x] Mở khóa (PermitAll) các endpoint: `/api/auth/login`, `/api/auth/refresh`, `/api/auth/register`.
- [x] Cấu hình CORS để cho phép Frontend (Vite) gọi API.
- [x] Khởi tạo `AuthenticationManager`, `PasswordEncoder` (BCrypt).

## JWT Service & Filters
- [x] Thiết lập Secret Key trong `application.properties` (hoặc biến môi trường).
- [x] Viết class `JwtService`: Hàm `generateToken()`, `extractUsername()`, `isTokenValid()`.
- [x] Nhúng Custom Claims (Role, UserId) vào Token khi generate — tránh DB lookup.
- [x] Viết bộ lọc `JwtAuthenticationFilter` (Kế thừa `OncePerRequestFilter`).
- [x] Xử lý lấy token từ header `Authorization: Bearer`.
- [x] Xác thực và thiết lập `SecurityContextHolder`.

## DTO & Logic Đăng nhập
- [x] Tạo các DTO: `LoginRequest`, `RegisterRequest`, `AuthResponse`, `RefreshTokenRequest`.
- [x] Xây dựng Entity `RefreshToken` và bảng `auth_refresh_tokens`.
- [x] Viết Repository `RefreshTokenRepository` hỗ trợ câu lệnh **UPSERT** gốc (Native Query).
- [x] `AuthUser` implement `UserDetailsService` tích hợp Spring Security trực tiếp (dùng email làm username).
- [x] Viết `AuthService` triển khai logic register, login (cast Principal), refresh, logout.
- [x] Xây dựng `AuthController` với các API: `/register`, `/login`, `/refresh`, `/logout`.

## Kiểm thử & Seeding
- [x] `DataSeeder` tự động tạo 4 Roles (SUPER_ADMIN, ADMIN, TEACHER, STUDENT) khi khởi động.
- [x] `DataSeeder` tự động tạo tài khoản Admin mặc định: `admin@sms.edu` / `Admin@123`.
- [x] Fix Lombok annotation processor trong `maven-compiler-plugin` — `mvn compile` thành công.
- [ ] Test API Login qua Postman, kiểm tra Token trả về.
- [ ] Kiểm tra lỗi 401 Unauthorized khi truy cập endpoint không có quyền.
