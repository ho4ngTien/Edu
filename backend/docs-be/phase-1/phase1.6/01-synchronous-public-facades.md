# Phase 1.6 — Thành phần 01: Synchronous Public Facades & Query Ports

Tài liệu này đặc tả quy chuẩn thiết kế các giao diện đồng bộ (Synchronous Public APIs) cho phép các module giao tiếp trực tiếp trong cùng một tiến trình JVM mà không làm rò rỉ chi tiết triển khai nội bộ.

---

## 1. Cấu Trúc Đặt Interface Công Khai

Mỗi module chỉ công khai các hợp đồng gọi trong package:
```
com.edu.<module>.api
├── dto/
│   ├── <Action>Command.java              # Input parameter DTO (Java Record)
│   └── <Entity>View.java                 # Read model DTO (Java Record)
└── <Module>PublicQuery.java              # Public interface
```

### Ví Dụ Thực Tế Trong Dự Án:
Module `organization` cần kiểm tra xem một tài khoản có tồn tại và đang hoạt động hay không trước khi tạo Membership.
- Module `organization` định nghĩa Outbound Port:
  ```java
  package com.edu.organization.application.port;

  import com.edu.shared.kernel.domain.identifier.UserId;
  import java.util.Optional;

  public interface OrganizationAccountQuery {
      Optional<AccountSummary> findActiveAccountById(UserId userId);
      record AccountSummary(UserId userId, String username, String email, boolean active) {}
  }
  ```
- Module `identity` triển khai Adapter kết nối vào repository nội bộ của nó:
  ```java
  package com.edu.identity.application.port;

  @Component
  public class IdentityAccountQueryAdapter implements OrganizationAccountQuery {
      private final UserJpaRepository userRepository;

      public Optional<AccountSummary> findActiveAccountById(UserId userId) {
          return userRepository.findById(userId.value())
              .filter(u -> u.getStatus() == UserStatus.ACTIVE)
              .map(u -> new AccountSummary(userId, u.getUsername(), u.getEmail(), true));
      }
  }
  ```

---

## 2. Các Nguyên Tắc Thiết Kế Bắt Buộc

1. **Sử dụng Java Record cho DTO**:
   - DTO phải là bất biến (`immutable`).
   - Tự động có `equals()`, `hashCode()`, `toString()` và không thể bị sửa đổi trạng thái sau khi khởi tạo.
2. **Không trả về JPA Entities**:
   - Ngăn chặn triệt để lỗi `LazyInitializationException` và việc vô tình sửa đổi dữ liệu qua Dirty Checking.
3. **Độc lập về Transaction**:
   - Khi Module A gọi Module B qua Facade, cuộc gọi diễn ra trong transaction hiện tại của Module A (nếu có), trừ khi được cấu hình `@Transactional(propagation = Propagation.REQUIRES_NEW)`.
