# ADR-004: Generalized Rule Engine

- **Mã Quyết Định**: ADR-004
- **Trạng Thái**: Accepted
- **Phạm Vi**: Đánh giá & Thẩm định Quy tắc Nghiệp vụ

---

## 1. Bối Cảnh (Context)

Một trong những thách thức phức tạp nhất của nền tảng E-Edu là sự khác biệt lớn về quy tắc nghiệp vụ giữa các mô hình giáo dục:
- Quy tắc điều kiện tiên quyết: "Phải đạt môn Toán cao cấp 1 (điểm >= 5.0) mới được đăng ký môn Toán cao cấp 2".
- Quy tắc điểm danh: "Vắng quá 20% tổng số buổi học thì cấm thi".
- Quy tắc tốt nghiệp: "Tích lũy tối thiểu 130 tín chỉ, GPA >= 2.0, hoàn thành chứng chỉ GDTC và Tin học".
Nếu lập trình viên hard-code các điều kiện này bằng các khối lệnh `if-else` trong tầng domain hoặc application service, mã nguồn sẽ trở nên phân mảnh, khó bảo trì và không thể cho phép quản trị viên các trường tự cấu hình chính sách của trường mình.

---

## 2. Quyết Định (Decision)

E-Edu quyết định xây dựng module **`rule-engine`** tổng quát:
1. `rule-engine` là một module độc lập, chuyên xử lý việc thẩm định biểu thức quy tắc (Rule Evaluation).
2. Quy tắc được đặc tả dưới dạng cấu trúc biểu thức (AST / JSON-based Rule Expression) với các toán tử logic chuẩn (`AND`, `OR`, `NOT`, `EQUALS`, `GREATER_THAN`, `IN`).
3. **Giới hạn an toàn nghiêm ngặt**:
   - Chỉ cho phép tập toán tử và kiểu dữ liệu được phê duyệt.
   - Nghiêm cấm thực thi mã tùy ý (arbitrary Java bytecode injection hoặc dynamic SQL execution) để bảo vệ an ninh hệ thống.
4. Có cơ chế versioning và audit log cho mọi thay đổi quy tắc.
5. Các module khác (`enrollment`, `attendance`, `assessment`, `certification`) đóng vai trò là Client của `rule-engine`: nạp dữ liệu ngữ cảnh (Context Variables) và nhận kết quả thẩm định (`EvaluationResult: PASSED / FAILED` kèm danh sách lý do vi phạm).

---

## 3. Hệ Quả & Đánh Đổi (Consequences)

### Lợi ích:
- Tách rời hoàn toàn logic thẩm định điều kiện ra khỏi mã nguồn nghiệp vụ chính.
- Cho phép mở rộng và hỗ trợ đa dạng mô hình giáo dục thông qua cấu hình.
- Khả năng kiểm thử độc lập cao với độ chính xác xác thực (Deterministic Testing).

### Thách thức & Biện pháp:
- Cần thiết kế cú pháp biểu thức thân thiện và có parser thẩm định cú pháp hợp lệ trước khi lưu trữ vào CSDL.
