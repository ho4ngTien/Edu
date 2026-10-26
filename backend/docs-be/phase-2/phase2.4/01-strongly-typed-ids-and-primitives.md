# Phase 2.4 — Thành phần 01: Strongly Typed IDs & Primitives

Tài liệu này đặc tả thiết kế định danh có kiểu mạnh (Strongly Typed IDs) bằng Java Record trong module `shared-kernel`.

---

## 1. Vấn Đề Khi Sử Dụng UUID Nguyên Thủy

Nếu sử dụng trực tiếp kiểu `java.util.UUID` cho tất cả các tham số:
```java
// Nguy cơ truyền nhầm thứ tự ID rất cao mà compiler không thể phát hiện!
void assignTeacher(UUID organizationId, UUID teacherId, UUID classSectionId);
// Lập trình viên vô tình gọi:
assignTeacher(teacherId, organizationId, classSectionId); // Biên dịch vẫn PASS nhưng chạy SAI!
```

---

## 2. Thiết Kế Bằng Java Record

Bọc `UUID` bên trong các Record bất biến chuyên biệt:

```java
package com.edu.shared.kernel.domain.identifier;

import java.util.Objects;
import java.util.UUID;

public record UserId(UUID value) {
    public UserId {
        Objects.requireNonNull(value, "UserId value không được phép null");
    }

    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(UUID value) {
        return new UserId(value);
    }

    public static UserId fromString(String str) {
        return new UserId(UUID.fromString(str));
    }
}
```

Tương tự cho `OrganizationId`:
```java
package com.edu.shared.kernel.domain.identifier;

import java.util.Objects;
import java.util.UUID;

public record OrganizationId(UUID value) {
    public OrganizationId {
        Objects.requireNonNull(value, "OrganizationId value không được phép null");
    }

    public static OrganizationId generate() {
        return new OrganizationId(UUID.randomUUID());
    }

    public static OrganizationId of(UUID value) {
        return new OrganizationId(value);
    }

    public static OrganizationId fromString(String str) {
        return new OrganizationId(UUID.fromString(str));
    }
}
```

### Lợi ích thu được:
1. **Type Safety tại thời điểm biên dịch**: Nếu truyền `OrganizationId` vào chỗ yêu cầu `UserId`, `javac` sẽ báo lỗi ngay lập tức.
2. **Tự động bất biến (Immutable)**: Record tự sinh `equals()`, `hashCode()`, `toString()` và không có setter.
3. **Fail-Fast Validation**: Ném `NullPointerException` ngay tại constructor nếu truyền giá trị `null`.
