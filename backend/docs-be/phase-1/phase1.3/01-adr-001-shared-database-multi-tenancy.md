# ADR-001: Shared Database Multi-Tenancy

- **Mã Quyết Định**: ADR-001
- **Trạng Thái**: Accepted
- **Phạm Vi**: Toàn bộ hệ thống Backend E-Edu

---

## 1. Bối Cảnh (Context)

Hệ thống E-Edu phục vụ đồng thời nhiều tổ chức giáo dục (các trường phổ thông, trường đại học, học viện và trung tâm đào tạo). Mỗi tổ chức là một khách hàng (tenant) độc lập về mặt quản trị và dữ liệu.
Hệ thống cần lựa chọn mô hình kiến trúc đa tổ chức (Multi-Tenancy) phù hợp:
1. **Lựa chọn A — Database-per-tenant**: Mỗi trường học sở hữu một database vật lý riêng.
   - *Ưu điểm*: Cô lập dữ liệu vật lý tuyệt đối.
   - *Nhược điểm*: Chi phí tài nguyên hạ tầng cực lớn khi có hàng ngàn trường; độ phức tạp vận hành và chạy migration Flyway cho hàng ngàn DB là quá tải.
2. **Lựa chọn B — Schema-per-tenant**: Dùng chung 1 DB, mỗi trường học một schema.
   - *Ưu điểm*: Phân tách logic tốt.
   - *Nhược điểm*: PostgreSQL connection pool và Flyway migration gặp giới hạn về số lượng schema khi scale; không thuận lợi cho kiến trúc Modular Monolith vốn đã dùng schema cho module.
3. **Lựa chọn C — Shared Database, Shared Tables (Row-level Isolation)**: Dùng chung 1 DB, các bảng chứa cột `organization_id`.

---

## 2. Quyết Định (Decision)

E-Edu quyết định chọn **Lựa chọn C: Shared Database, Shared Tables** với sự kiểm soát chặt chẽ ở tầng ứng dụng và cơ sở dữ liệu:
1. Tất cả các bảng dữ liệu thuộc phạm vi tổ chức bắt buộc có cột `organization_id UUID NOT NULL`.
2. Giá trị `organization_id` tuyệt đối không được tin cậy từ input của client mà phải được phân giải và xác thực thông qua `SecurityContext` / `TenantContext`.
3. Bắt buộc có các ràng buộc `UNIQUE` kết hợp `(organization_id, business_code)`.
4. Triển khai automated tenant isolation tests nhằm ngăn chặn triệt để nguy cơ rò rỉ dữ liệu chéo tổ chức (IDOR).

---

## 3. Hệ Quả & Đánh Đổi (Consequences)

### Lợi ích:
- Tối ưu chi phí hạ tầng: Chỉ cần vận hành một PostgreSQL cluster duy nhất.
- Vận hành và migration tinh gọn: Toàn bộ quá trình nâng cấp Flyway diễn ra trong một chu kỳ build duy nhất.
- Người dùng linh hoạt: Một tài khoản có thể tham gia nhiều tổ chức mà không cần đồng bộ dữ liệu phức tạp.

### Thách thức & Biện pháp:
- **Nguy cơ truy vấn thiếu điều kiện `organization_id`**: Kiểm soát bằng các repository methods có phạm vi `findByOrganizationIdAnd...`, kiểm tra quyền hạn trước khi đọc/ghi, và viết unit test phủ sóng các kịch bản rò rỉ dữ liệu.
