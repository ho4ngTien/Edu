# Checkpoint 2.7.9 — Security Review & Acceptance Matrix

> **Thuộc Phase**: [Phase 2.7 — Authorization & Access Control](README.md)  
> **Trạng thái**: 📋 Khung thẩm định sẵn sàng (Review Framework Approved)  
> **Tiêu chí nghiệm thu**: `AC-2.7.9`

---

## 1. Mục tiêu Checkpoint

Xác lập khung thẩm định và ma trận nghiệm thu an ninh cuối cùng cho toàn bộ phân hệ **Authorization & Access Control (Phase 2.7)**. Tài liệu quy định 10 tiêu chuẩn bắt buộc (R1 đến R10), các quy tắc ra quyết định nghiệm thu kỹ thuật (`ACCEPTED`, `ACCEPTED WITH CONDITIONS`, `REJECTED`), và danh mục rà soát an ninh theo chuẩn OWASP API Security Top 10 trước khi chính thức đưa vào vận hành.

---

## 2. Bảng Ma trận Rà soát Toàn diện (Checkpoints AC-2.7.1 -> AC-2.7.9)

| Checkpoint | Nội dung cốt lõi | Bằng chứng kỹ thuật bắt buộc |
| :---: | :--- | :--- |
| **AC-2.7.1** | Architecture Decisions & ADRs | ADR-017 đến ADR-020 được lưu trữ trong git, thống nhất với mã nguồn. |
| **AC-2.7.2** | Permission Model & Catalog | Danh mục quyền chuẩn hóa (`DOMAIN_RESOURCE_ACTION`), phân định SYSTEM/ORGANIZATION. |
| **AC-2.7.3** | Role & Permission Persistence | Flyway migration `V6` chạy thành công; các ràng buộc FK, Unique, Index và Seed data đầy đủ. |
| **AC-2.7.4** | Organization-Level Authorization | Server-side `TenantContext` resolution, kiểm tra membership `ACTIVE`, chặn rò rỉ tenant. |
| **AC-2.7.5** | Method-Level Security | `@EnableMethodSecurity` kích hoạt; `@PreAuthorize` bao phủ toàn bộ application service. |
| **AC-2.7.6** | Resource-Level Authorization | Query-level tenant filtering (`WHERE organization_id = :orgId`); kiểm tra phạm vi thực thể. |
| **AC-2.7.7** | Authorization Error Mapping | Thống nhất mã lỗi 401, 403, 404; serialize chuẩn định dạng JSON `ApiErrorResponse`. |
| **AC-2.7.8** | Authorization Integration Tests | Toàn bộ 12 test cases (IT-01 -> IT-12) chạy thành công trên PostgreSQL Testcontainers. |
| **AC-2.7.9** | Security Review & Acceptance | 10 tiêu chuẩn R1–R10 được thẩm định; không có lỗ hổng bảo mật nghiêm trọng chưa xử lý. |

---

## 3. 10 Tiêu chuẩn Nghiệm thu Bắt buộc (The 10 Golden Rules: R1 -> R10)

### Nhóm 1: Architecture & Data Model Integrity
- **R1**: ADR-017 đến ADR-020 được phê duyệt chính thức và không có bất kỳ mâu thuẫn nào với thiết kế code.
- **R2**: Danh mục Permission Catalog, bảng cấu trúc Roles và bảng phân quyền khớp 100% với schema PostgreSQL trong migration `V6`.
- **R3**: Không tạo bất kỳ foreign key vật lý chéo schema nào giữa `organization` và các module khác; không import trực tiếp Entity/Repository của module khác.

### Nhóm 2: Security & Tenant Isolation Enforcement
- **R4**: Ngữ cảnh tổ chức (`TenantContext`) được xác minh độc lập từ server, không bao giờ tin cậy mù quáng các header do client tự cung cấp.
- **R5**: Chuỗi thẩm định quyền được thực thi nghiêm ngặt theo đúng thứ tự: `Token Validity` -> `Membership Status` -> `Method Permission` -> `Resource Scope`.
- **R6**: Method-level security được bật trên toàn hệ thống; không có bất kỳ phương thức nghiệp vụ nhạy cảm nào bị bỏ qua hoặc bị bypass do proxy self-invocation.
- **R7**: System-level roles không được phép tự động vượt rào cô lập của tenant nếu không có cơ chế thẩm định đặc quyền và ghi log kiểm toán.

### Nhóm 3: Testing & Operational Readiness
- **R8**: Bộ integration tests bao phủ đầy đủ cả kịch bản thành công lẫn các kịch bản cố tình truy cập trái phép hoặc tráo ID tài nguyên.
- **R9**: Lệnh `mvn clean verify` chạy thành công với exit code 0; toàn bộ test cases đạt PASS, không có bài kiểm tra quan trọng nào bị skip.
- **R10**: Không còn bất kỳ lỗ hổng an ninh nào chưa xử lý theo danh mục OWASP API Security Top 10 (đặc biệt là BOLA, Broken Authentication và BFLA).

---

## 4. Quy tắc Quyết định Nghiệm thu (Decision Protocol)

Hội đồng thẩm định hoặc Lead Developer áp dụng một trong ba mức quyết định sau:

### 🟢 1. ACCEPTED (Nghiệm thu Đạt)
- Đạt 10/10 tiêu chí (R1 đến R10).
- Toàn bộ unit tests, integration tests và ArchUnit tests chạy thành công.
- Cơ sở dữ liệu PostgreSQL thực thi migration `V6` hoàn hảo, không có lỗi xung đột.
- Hệ thống sẵn sàng cho việc phát triển các phân hệ nghiệp vụ tiếp theo (People, Curriculum, Academic).

### 🟡 2. ACCEPTED WITH CONDITIONS (Nghiệm thu Có Điều kiện)
- Chỉ áp dụng khi đạt tối thiểu 9/10 tiêu chí và thiếu sót duy nhất thuộc về tài liệu bổ trợ hoặc tối ưu hóa hiệu năng nhỏ.
- Tuyệt đối **không được áp dụng** mức này nếu có bất kỳ lỗi nào liên quan đến rò rỉ dữ liệu chéo tenant (Tenant Isolation failure) hoặc lỗi bỏ qua kiểm tra quyền.
- Phải lập danh sách tồn đọng (Action Items) có hạn xử lý cụ thể trong vòng 48 giờ.

### 🔴 3. REJECTED / BLOCKED (Từ chối / Chặn Phát hành)
- Bị kích hoạt ngay lập tức nếu phát hiện bất kỳ dấu hiệu nào sau đây:
  1. Người dùng thuộc Tổ chức A có thể đọc hoặc sửa đổi dữ liệu của Tổ chức B (IDOR / Cross-Tenant Leakage).
  2. Bất kỳ phương thức Application Service nào chứa thao tác ghi dữ liệu nhạy cảm mà thiếu `@PreAuthorize`.
  3. Vi phạm ranh giới module (ArchUnit failure) hoặc tạo cross-schema FK.
  4. Integration test thất bại hoặc bị disabled/skipped để đối phó build.

---

## 5. Danh mục Rà soát An ninh OWASP API Security Top 10

| Hạng mục OWASP | Rủi ro cụ thể | Giải pháp bảo vệ của E-Edu | Trạng thái |
| :--- | :--- | :--- | :---: |
| **API1: Broken Object Level Authorization (BOLA)** | User đổi ID để xem khóa học/điểm của người khác hoặc trường khác. | Query-level tenant filtering (`WHERE organization_id = :orgId`) kết hợp Resource Authorization Check. | ✅ Protected |
| **API2: Broken Authentication** | Token giả mạo, refresh token bị đánh cắp và tái sử dụng. | Ký số JWT HMAC-SHA256, Refresh Token Rotation & Reuse Detection (thu hồi cả chuỗi). | ✅ Protected |
| **API3: Broken Object Property Level Authorization** | User gửi kèm các trường nhạy cảm trong JSON body (Mass Assignment). | DTO tách biệt cho từng thao tác; ArchUnit cấm các trường nhạy cảm trong DTO. | ✅ Protected |
| **API4: Unrestricted Resource Consumption** | Brute-force mật khẩu hoặc flood request. | Giới hạn ký tự input, chuẩn bị Rate Limiting tại Gateway / Filter. | ✅ Protected |
| **API5: Broken Function Level Authorization (BFLA)** | Sinh viên gọi endpoint dành riêng cho giáo viên hoặc admin. | Method-Level Security `@PreAuthorize("hasAuthority('...')")` tại tầng Service. | ✅ Protected |
| **API8: Security Misconfiguration** | Lộ stack trace, thông tin database hoặc cấu hình mặc định. | `GlobalExceptionHandler` chặn bắt toàn bộ lỗi nội bộ, ẩn chi tiết lỗi trên production. | ✅ Protected |

---

## 6. Kết luận & Hướng phát triển Tiếp theo

Khi Phase 2.7 được triển khai và nghiệm thu thành công, nền tảng E-Edu sẽ sở hữu một hệ thống bảo vệ an ninh vững chắc, sẵn sàng đón nhận các phân hệ nghiệp vụ phức tạp của Phase 4, Phase 5 và Phase 6 (`people`, `curriculum`, `academic`, `enrollment`, `assessment`, `rule-engine`).
