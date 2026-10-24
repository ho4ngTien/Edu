# E-Edu — Phase 1.3: Architecture Decisions

Mục tiêu của phase này là chốt những quyết định kiến trúc nền tảng cho E-Edu trước khi bắt đầu thiết kế database và triển khai code.

Với định hướng **Modular Monolith + Java 21 + Spring Boot + PostgreSQL**, dự án ưu tiên tính nhất quán, khả năng mở rộng và ranh giới module rõ ràng.

---

## 1. Bốn quyết định kiến trúc cốt lõi

### AD-01 — Multi-Tenancy
- **Bối cảnh**: E-Edu phục vụ nhiều tổ chức (trường phổ thông, đại học, trung tâm ngoại ngữ) trên cùng hạ tầng.
- **Quyết định**: **Shared database + shared tables**, phân tách theo `organization_id`.
- **Nguyên tắc**:
  - Dùng chung một PostgreSQL database.
  - Các bản ghi thuộc phạm vi tổ chức phải có `organization_id`.
  - Backend bắt buộc xác minh quyền truy cập tổ chức trước khi truy vấn hoặc thay đổi dữ liệu; không tin cậy `organization_id` từ client.
  - Áp dụng kiểm thử tự động (tenant isolation test) để ngăn truy cập chéo dữ liệu.

### AD-02 — PostgreSQL Schema Strategy
- **Bối cảnh**: Cần ranh giới dữ liệu rõ ràng giữa các module mà không cần tách nhiều database server.
- **Quyết định**: **Tách PostgreSQL schema riêng theo từng module** (`identity`, `organization`, `academic`, `assessment`...).
- **Nguyên tắc**:
  - Mỗi module sở hữu schema, bảng, repository và Flyway migration riêng.
  - Không dùng chung JPA entity giữa các module; không join cross-schema tùy tiện.
  - Hạn chế tối đa cross-schema foreign key.

### AD-03 — Kiểm soát ranh giới Module
- **Bối cảnh**: Cần ngăn chặn việc phá vỡ kiến trúc (cyclic dependency, rò rỉ ranh giới).
- **Quyết định**: **Maven Multi-Module + ArchUnit**.
- **Nguyên tắc**:
  - Maven tách từng module thành subproject riêng với `pom.xml` độc lập.
  - ArchUnit kiểm tra các quy tắc phụ thuộc bằng automated tests trong quy trình CI.

### AD-04 — Mô hình cấu hình nghiệp vụ
- **Bối cảnh**: Cần hỗ trợ linh hoạt nhiều mô hình giáo dục (thang điểm, điều kiện hoàn thành, quy định điểm danh, tín chỉ).
- **Quyết định**: **Rule engine tổng quát ngay từ đầu** (kết hợp typed configuration).
- **Nguyên tắc**:
  - Xây dựng một module `rule-engine` có API, mô hình biểu thức, cơ chế thẩm định và đánh giá độc lập.
  - Giới hạn phạm vi: chỉ hỗ trợ tập toán tử, hàm và kiểu dữ liệu được phê duyệt; tuyệt đối không cho phép thực thi mã tùy ý (arbitrary Java/SQL).
  - Có cơ chế versioning và audit log cho mọi thay đổi quy tắc.

---

## 2. Tổng hợp quyết định & Phân tích hệ quả

| ADR | Quyết định | Trạng thái |
| :--- | :--- | :--- |
| **ADR-001** | Shared database + shared tables, phân tách theo `organization_id` | **Accepted** |
| **ADR-002** | PostgreSQL schema riêng theo từng module | **Accepted** |
| **ADR-003** | Maven Multi-Module + ArchUnit | **Accepted** |
| **ADR-004** | Rule engine tổng quát ngay từ đầu | **Accepted** |

### Hệ quả quan trọng và biện pháp kiểm soát:

1. **Về Multi-Tenancy (AD-01)**:
   - Các truy vấn đọc, cập nhật, xóa phải áp dụng phạm vi tổ chức phù hợp.
   - Với các quan hệ cùng tổ chức, cân nhắc composite foreign key `(id, organization_id)` hoặc ràng buộc tương đương.
   - Kiểm thử tự động đảm bảo User của Tenant A không thể đọc/ghi dữ liệu Tenant B.

2. **Về Schema riêng theo Module (AD-02)**:
   - Tên schema và bảng: `identity.users`, `academic.class_sections`, `assessment.assessments`...
   - Schema riêng giúp thấy rõ quyền sở hữu, nhưng không tự ngăn được cross-schema query nếu dùng chung DB user; do đó vẫn phải kiểm soát chặt chẽ bằng ArchUnit và review code.

3. **Về Maven + ArchUnit (AD-03)**:
   - Bắt buộc chạy ArchUnit trong CI, chặn merge pull request nếu vi phạm dependency rules.

4. **Về Rule Engine (AD-04)**:
   - Rule engine chỉ thẩm định điều kiện (Evaluation). Các module nghiệp vụ (`assessment`, `enrollment`) vẫn chịu trách nhiệm về dữ liệu, phân quyền và workflow.

---

## 3. Architecture Decision Records (ADR)

### ADR-001: Shared Database Multi-Tenancy
- **Status**: Accepted
- **Context**: E-Edu supports multiple educational organizations and education models. The system must share infrastructure while isolating data belonging to each organization.
- **Decision**: Use one PostgreSQL database with shared tables. Organization-scoped records must carry `organization_id` where applicable.
- **Consequences**:
  - Infrastructure and migration operations are simpler than database-per-tenant.
  - Users may participate in multiple organizations.
  - Application services must authorize organization access on every relevant operation.
  - Cross-tenant references must be prevented through application validation and appropriate database constraints.
  - Automated tenant-isolation tests are mandatory.

### ADR-002: PostgreSQL Schema Per Module
- **Status**: Accepted
- **Context**: E-Edu uses a Modular Monolith. Each module should own its data without introducing separate database servers or services.
- **Decision**: Use one PostgreSQL database with a dedicated schema for each module (e.g., `identity`, `organization`, `people`, `academic`, `assessment`).
- **Consequences**:
  - Table ownership is explicit and visible.
  - Each module maintains its own Flyway migrations and persistence code.
  - Cross-schema queries and foreign keys must be strictly limited and documented.
  - Modules must not directly use another module's internal repositories or JPA entities.

### ADR-003: Maven Multi-Module and ArchUnit
- **Status**: Accepted
- **Context**: E-Edu requires compile-time module separation and automated enforcement of dependency rules.
- **Decision**: Organize the codebase as a Maven multi-module project and use ArchUnit tests to validate package and dependency constraints in CI.
- **Consequences**:
  - Modules can be compiled and tested independently within the repository.
  - Module APIs become explicit.
  - Circular dependencies and unauthorized package dependencies are detected automatically during CI builds.

### ADR-004: General-Purpose Business Rule Engine
- **Status**: Accepted
- **Context**: E-Edu supports different educational models with configurable grading, attendance, progression, and completion requirements.
- **Decision**: Build a reusable rule-engine capability from the beginning. Domain modules invoke it through a stable application API with validated inputs and explicit rule versions.
- **Consequences**:
  - Business rules can be configured and versioned without embedding rigid policy code in domain modules.
  - Requires a defined expression model, validation, execution limits, and audit trails.
  - Rules must not execute arbitrary Java, SQL, scripts, or unrestricted external calls.
  - Initial scope supports a restricted, typed set of conditions, comparisons, and logical operators.
