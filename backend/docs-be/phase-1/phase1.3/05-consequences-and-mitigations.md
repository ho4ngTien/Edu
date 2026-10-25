# Phase 1.3 — Thành phần 05: Consequences & Risk Mitigations

Tài liệu này tổng hợp ma trận phân tích rủi ro, đánh đổi kỹ thuật (Trade-offs) và các biện pháp kiểm soát cho toàn bộ các quyết định kiến trúc từ ADR-001 đến ADR-004.

---

## 1. Ma Trận Đánh Đổi Kỹ Thuật (Architectural Trade-Off Matrix)

| Quyết định | Lợi ích chính thu được | Đánh đổi / Rủi ro tiềm ẩn | Biện pháp giảm thiểu rủi ro (Mitigation) |
| :--- | :--- | :--- | :--- |
| **ADR-001: Shared DB Multi-Tenancy** | Chi phí hạ tầng thấp, vận hành Flyway tập trung, hỗ trợ người dùng đa tổ chức. | Nguy cơ truy vấn rò rỉ dữ liệu chéo trường học (IDOR). | 1. Bắt buộc `organization_id NOT NULL` trên mọi bảng.<br/>2. Trích xuất ID tổ chức từ SecurityContext.<br/>3. Automated tenant-isolation tests trong CI. |
| **ADR-002: PostgreSQL Schema Per Module** | Phân định quyền sở hữu bảng rõ ràng, chuẩn bị sẵn lộ trình phân rã microservice. | Không thể dùng SQL JOIN chéo schema; hiệu năng tổng hợp báo cáo. | 1. Tra cứu qua Public Query Ports.<br/>2. Gom nhóm dữ liệu ở application layer.<br/>3. Xây dựng Read Model chuyên dụng cho reporting. |
| **ADR-003: Maven + ArchUnit** | Cưỡng chế ranh giới phụ thuộc tự động 100%, chặn code xấu tại thời điểm compile & build. | Phải quản lý nhiều file `pom.xml`, thời gian chạy build tăng nhẹ. | 1. Quản lý phiên bản tập trung tại Root Parent POM.<br/>2. Tối ưu hóa rule ArchUnit chỉ quét các package trọng yếu. |
| **ADR-004: Generalized Rule Engine** | Hỗ trợ linh hoạt mọi quy tắc của K-12, Đại học, Trung tâm mà không sửa code core. | Độ phức tạp khi xây dựng bộ parser và evaluator quy tắc. | 1. Giới hạn tập toán tử an toàn.<br/>2. Không cho phép dynamic code execution.<br/>3. Cung cấp bộ unit test mẫu cho từng use case. |

---

## 2. Kế Hoạch Đánh Giá Kiến Trúc Định Kỳ

Dự án cam kết rà soát lại các quyết định kiến trúc:
1. **Sau khi hoàn thành Phase 2**: Kiểm chứng thực tế sự hoạt động của Multi-Schema, ArchUnit và Security Filter Chain.
2. **Sau khi triển khai 4 module Education Core (Phase 5)**: Đánh giá hiệu năng của cơ chế giao tiếp liên module qua Public Ports.
3. **Trước khi Go-Live**: Thực hiện pentest và kiểm tra tải (load testing) đối với khả năng cô lập dữ liệu tổ chức và tốc độ phản hồi của hệ thống.
