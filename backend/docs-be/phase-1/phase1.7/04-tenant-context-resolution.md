# Phase 1.7 — Thành phần 04: Tenant Context Resolution & IDOR Prevention

Tài liệu này đặc tả cơ chế tự động phân giải ngữ cảnh tổ chức (Tenant Context Resolution) thông qua HTTP Header `X-Organization-Id` và các kỹ thuật phòng vệ ngăn chặn lỗi IDOR.

---

## 1. Cơ Chế Hoạt Động Của `TenantContext`

Trong môi trường đa luồng (Multi-threaded Web Environment) của Spring Boot, ngữ cảnh tổ chức được lưu trữ an toàn trong `ThreadLocal`:

```java
package com.edu.shared.kernel.domain.context;

import com.edu.shared.kernel.domain.identifier.OrganizationId;
import java.util.Optional;

public final class TenantContextHolder {
    private static final ThreadLocal<OrganizationId> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContextHolder() {}

    public static void setTenantId(OrganizationId organizationId) {
        CURRENT_TENANT.set(organizationId);
    }

    public static Optional<OrganizationId> getTenantId() {
        return Optional.ofNullable(CURRENT_TENANT.get());
    }

    public static OrganizationId requireTenantId() {
        return getTenantId().orElseThrow(() -> 
            new IllegalStateException("Không tìm thấy OrganizationId trong TenantContext!"));
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
```

> [!WARNING]
> Luôn gọi `TenantContextHolder.clear()` trong khối `finally` của filter để tránh hiện tượng rò rỉ bộ nhớ (ThreadLocal Memory Leak) khi Tomcat tái sử dụng Thread từ Thread Pool.

---

## 2. Quy Trình Phân Giải Tenant và Xác Minh Quyền Hạn

```
1. Client gửi request:
   Header: Authorization: Bearer <JWT>
   Header: X-Organization-Id: a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11

2. TenantResolutionFilter chặn request:
   a. Lấy UserId từ SecurityContext đã xác thực (từ JWT claim 'sub').
   b. Đọc giá trị header X-Organization-Id.
   c. Nếu API yêu cầu tenant context mà không có header -> Trả lỗi 400 Bad Request.
   d. Gọi Public Port của module organization để xác minh:
      "UserId này có membership ACTIVE tại OrganizationId này không?"
      -> Nếu KHÔNG: Trả lỗi 403 Forbidden ("Bạn không phải thành viên của tổ chức này").
      -> Nếu CÓ: Nạp OrganizationId vào TenantContextHolder.

3. Controller & Application Service xử lý:
   - Tự động lấy OrganizationId từ TenantContextHolder.requireTenantId().
   - Truy vấn database luôn kèm điều kiện WHERE organization_id = :tenantId.
```

---

## 3. Phòng Chống Triệt Để Lỗ Hổng IDOR

1. **Không tin cậy ID từ Client**:
   - Khi tạo lớp học, bài thi hoặc nhập điểm: API không lấy `organization_id` từ JSON Body do client gửi lên.
   - Luôn gán: `entity.setOrganizationId(TenantContextHolder.requireTenantId())`.
2. **Kiểm tra quyền sở hữu khi truy vấn theo ID (`GET /api/v1/classes/{classId}`)**:
   - Repository bắt buộc truy vấn:
     ```java
     Optional<ClassSectionEntity> findByIdAndOrganizationId(UUID id, UUID organizationId);
     ```
   - Nếu `classId` tồn tại nhưng thuộc trường khác, câu lệnh trả về `Optional.empty()` $\to$ Hệ thống ném `ResourceNotFoundException` (404), kẻ tấn công không thể biết tài nguyên có tồn tại hay không.
