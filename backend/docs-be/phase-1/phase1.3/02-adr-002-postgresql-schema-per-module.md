# ADR-002: PostgreSQL Schema Per Module

- **Mã Quyết Định**: ADR-002
- **Trạng Thái**: Accepted
- **Phạm Vi**: Toàn bộ hệ thống Backend E-Edu

---

## 1. Bối Cảnh (Context)

Trong kiến trúc Modular Monolith, một thách thức lớn nhất là nguy cơ các lập trình viên vô tình hoặc cố ý thực hiện các câu lệnh SQL JOIN chéo giữa bảng của các module khác nhau, hoặc chia sẻ JPA Entity giữa các module. Điều này nhanh chóng biến hệ thống thành một khối nguyên khối rối rắm (Big Ball of Mud) và phá hủy tính độc lập của từng module.

---

## 2. Quyết Định (Decision)

E-Edu áp dụng chiến lược **PostgreSQL Schema Per Module**:
1. Trong database vật lý duy nhất `edu`, tạo 12 PostgreSQL schemas độc lập tương ứng với từng module Bounded Context:
   - `identity`, `organization`, `configuration`, `people`, `curriculum`, `academic`, `enrollment`, `teaching`, `attendance`, `assessment`, `certification`, `rule_engine`.
2. Mỗi module chịu trách nhiệm toàn quyền với schema của mình:
   - Tự quản lý các bảng, chỉ mục và tài nguyên Flyway migrations.
   - Không chia sẻ JPA Entity hay Repository sang module khác.
3. **Cấm tuyệt đối Physical Foreign Key xuyên schema**: Mọi tham chiếu liên module đều sử dụng Logical ID (`UUID`).

---

## 3. Hệ Quả & Đánh Đổi (Consequences)

### Lợi ích:
- Quyền sở hữu dữ liệu minh bạch: Nhìn vào tên schema là biết module nào chịu trách nhiệm (`academic.class_sections`, `people.students`).
- Dễ dàng kiểm soát bảo mật và cô lập schema.
- Chuẩn bị sẵn lộ trình phân rã Microservices nếu sau này có nhu cầu mở rộng quy mô.

### Thách thức & Biện pháp:
- **Không thể dùng SQL JOIN chéo schema để tạo báo cáo phức tạp**: Giải quyết bằng các cơ chế tra cứu qua Public Query Ports, tập hợp dữ liệu ở tầng application, hoặc xây dựng Read Model chuyên dụng cho reporting trong các phase sau.
