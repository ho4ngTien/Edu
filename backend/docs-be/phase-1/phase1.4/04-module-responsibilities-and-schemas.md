# Phase 1.4 — Thành phần 04: Module Responsibilities & Schema Catalog

Tài liệu này tổng hợp danh mục phân định trách nhiệm chi tiết, schema PostgreSQL tương ứng và cấu trúc thư mục package của 11 module nghiệp vụ trong E-Edu.

---

## 1. Bảng Tổng Hợp Chi Tiết 11 Module Nghiệp Vụ

| Module | Maven ArtifactId | PostgreSQL Schema | Java Root Package | Trách nhiệm chính |
| :--- | :--- | :--- | :--- | :--- |
| **`identity`** | `identity` | `identity` | `com.edu.identity` | Quản lý tài khoản đăng nhập, xác thực BCrypt, trạng thái tài khoản, JWT refresh tokens. |
| **`organization`**| `organization` | `organization` | `com.edu.organization` | Quản lý đơn vị tổ chức trường học, các đơn vị con, quan hệ thành viên và phân quyền tổ chức. |
| **`configuration`**| `configuration` | `configuration` | `com.edu.configuration` | Quản lý cấu hình tham số hệ thống và các chính sách đào tạo có kiểu theo từng tổ chức. |
| **`people`** | `people` | `people` | `com.edu.people` | Quản lý hồ sơ nhân khẩu học (`Person`), hồ sơ học sinh (`Student`), giáo viên (`Teacher`), người giám hộ. |
| **`curriculum`** | `curriculum` | `curriculum` | `com.edu.curriculum` | Quản lý khung chương trình đào tạo, phiên bản chương trình, danh mục môn học và môn trong chương trình. |
| **`academic`** | `academic` | `academic` | `com.edu.academic` | Quản lý năm học/học kỳ, lớp niên chế, đợt mở môn học (`CourseOffering`), lớp học phần, phòng học. |
| **`enrollment`** | `enrollment` | `enrollment` | `com.edu.enrollment` | Quản lý quy trình đăng ký/tiếp nhận vào chương trình và đăng ký lớp học phần của học viên. |
| **`teaching`** | `teaching` | `teaching` | `com.edu.teaching` | Phân công giảng dạy của giáo viên cho các lớp học phần và quản lý thời khóa biểu giảng dạy. |
| **`attendance`** | `attendance` | `attendance` | `com.edu.attendance` | Quản lý bảng điểm danh, điểm danh từng buổi học và thống kê tỷ lệ chuyên cần. |
| **`assessment`** | `assessment` | `assessment` | `com.edu.assessment` | Quản lý cơ cấu thang điểm, cấu trúc bài đánh giá, nhập điểm, tính điểm tổng kết và phúc khảo. |
| **`certification`**| `certification` | `certification` | `com.edu.certification` | Quản lý điều kiện công nhận hoàn thành khóa học, cấp văn bằng/chứng chỉ theo dõi lịch sử cấp bằng. |
| **`rule-engine`** | `rule-engine` | `rule_engine` | `com.edu.ruleengine` | Bộ thẩm định biểu thức quy tắc động (điều kiện tiên quyết, giới hạn đăng ký, điều kiện tốt nghiệp). |

---

## 2. Các Module Kỹ Thuật Bổ Trợ

| Module | Maven ArtifactId | Java Root Package | Vai trò kỹ thuật |
| :--- | :--- | :--- | :--- |
| **`boot`** | `boot` | `com.edu.boot` | Composition Root: Khởi động Spring Boot, cấu hình datasource, security filter chain, điều phối Flyway migration. |
| **`shared-kernel`**| `shared-kernel` | `com.edu.shared.kernel` | Base entities (`AuditableEntity`), Value Objects (`UserId`, `OrganizationId`), `DomainEvent`, `ApiErrorResponse`. |
| **`architecture-tests`**| `architecture-tests`| `com.edu.tests.architecture` | Bộ kiểm thử tự động với ArchUnit bảo vệ toàn bộ 9 quy tắc kiến trúc và phụ thuộc module. |
