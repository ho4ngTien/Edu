# ADR-003: Maven Multi-Module & ArchUnit Enforcement

- **Mã Quyết Định**: ADR-003
- **Trạng Thái**: Accepted
- **Phạm Vi**: Build Pipeline và Cấu trúc Mã nguồn E-Edu

---

## 1. Bối Cảnh (Context)

Các quy tắc kiến trúc (như "module A không được phụ thuộc module B", "không gọi trực tiếp repository nội bộ của module khác") nếu chỉ được ghi trong tài liệu hoặc hướng dẫn miệng sẽ rất nhanh chóng bị vi phạm khi đội ngũ phát triển mở rộng hoặc dưới áp lực tiến độ.
Hệ thống cần một cơ chế thực thi tự động (Automated Architectural Fitness Functions) ở cả 2 cấp độ:
- Cấp độ thời gian biên dịch (Compile-time): Ngăn việc import trái phép qua build tool.
- Cấp độ kiểm thử tự động (Test-time): Phân tích bytecode để kiểm tra ranh giới package và quan hệ phụ thuộc.

---

## 2. Quyết Định (Decision)

E-Edu quyết định kết hợp **Maven Multi-Module** và **ArchUnit**:
1. **Maven Multi-Module (Compile-time Boundary)**:
   - Tách từng Bounded Context thành một Maven submodule độc lập với `pom.xml` riêng.
   - Nếu module A không khai báo dependency tới module B trong `pom.xml`, trình biên dịch `javac` sẽ báo lỗi ngay lập tức nếu có lập trình viên cố gắng import class của B.
2. **ArchUnit Tests (Bytecode & Architecture Enforcement)**:
   - Xây dựng module `tests/architecture-tests` tích hợp thư viện ArchUnit.
   - Định nghĩa các quy tắc bất biến: cấm cyclic dependencies, cấm truy cập package internal từ bên ngoài, kiểm tra quy chuẩn đặt tên class.
   - Tích hợp vào lệnh `mvn clean verify` của CI/CD pipeline: Nếu vi phạm bất kỳ quy tắc nào, build sẽ bị FAILED và chặn merge pull request.

---

## 3. Hệ Quả & Đánh Đổi (Consequences)

### Lợi ích:
- Ranh giới kiến trúc được bảo vệ tự động 100%, không phụ thuộc vào cảm tính của con người.
- Phản hồi tức thì ngay trên IDE và máy local của lập trình viên.
- Duy trì tính kỷ luật kiến trúc lâu dài cho dự án.

### Thách thức & Biện pháp:
- Thời gian chạy build tăng thêm một chút do ArchUnit phải quét bytecode (thường mất từ 1-3 giây, hoàn toàn nằm trong mức chấp nhận được).
