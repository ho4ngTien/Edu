# Phase 1.4 — Thành phần 03: Package Visibility & Public API Encapsulation

Tài liệu này đặc tả các quy định nghiêm ngặt về phạm vi truy cập của các lớp (Access Modifiers) nhằm bảo vệ tính đóng gói và ranh giới an toàn cho từng module trong E-Edu.

---

## 1. Bảng Quy Định Phạm Vi Truy Cập (Access Modifiers)

| Package | Thành phần bên trong | Access Modifier | Được phép truy cập từ module khác? |
| :--- | :--- | :---: | :---: |
| **`com.edu.<module>.api`** | Public Query / Facade Interfaces | `public` | **CÓ** |
| **`com.edu.<module>.api.dto`** | Command / Result DTOs (Java Records) | `public` | **CÓ** |
| **`com.edu.<module>.application`** | Application Services (`@Service`) | `package-private` hoặc `public` nội bộ | **KHÔNG** |
| **`com.edu.<module>.domain.model`** | Domain Models, Value Objects | `package-private` | **KHÔNG** |
| **`com.edu.<module>.domain.repository`**| Domain Repository Interfaces | `package-private` | **KHÔNG** |
| **`com.edu.<module>.infrastructure`** | JPA Entities (`@Entity`), Repositories | `package-private` | **KHÔNG** |
| **`com.edu.<module>.infrastructure.web`**| REST Controllers (`@RestController`) | `package-private` | **KHÔNG** (Chỉ gọi qua HTTP) |

---

## 2. Vì Sao Cấm Chia Sẻ JPA Entity?

1. **Rò rỉ Hibernate Session**: Nếu Module A trả về một JPA Entity cho Module B, khi Module B truy cập thuộc tính Lazy Loading (`fetch = FetchType.LAZY`), Hibernate Session có thể đã bị đóng dẫn đến lỗi `LazyInitializationException`.
2. **Nguy cơ Dirty Checking ngoài ý muốn**: Module B có thể gọi setter trên Entity của Module A, khiến Hibernate tự động phát sinh câu lệnh SQL `UPDATE` vào bảng của Module A mà Module A không hề hay biết!
3. **Phá vỡ tính độc lập dữ liệu**: Cả 2 module cùng bị khóa chặt vào cấu trúc bảng vật lý, khiến việc refactor database trở nên cực kỳ rủi ro.

> **Giải pháp chuẩn:** Luôn ánh xạ JPA Entity sang Java Record DTO bất biến trước khi trả dữ liệu ra khỏi ranh giới của module!
