# Phase 1.2 — Thành phần 02: Dependency Graph & Rules

Tài liệu này đặc tả đồ thị phụ thuộc giữa các module trong E-Edu và các quy tắc kiến trúc bắt buộc để ngăn chặn sự xói mòn kiến trúc (Architectural Erosion).

---

## 1. Đồ Thị Phụ Thuộc Hợp Lệ (Mermaid DAG)

```mermaid
flowchart TD
    BOOT["boot (Composition Root)"]
    SHARED["shared-kernel"]

    subgraph Platform ["Platform Layer"]
        ID["identity"]
        ORG["organization"]
        CFG["configuration"]
    end

    subgraph Core ["Education Core"]
        PEOPLE["people"]
        CUR["curriculum"]
        ACAD["academic"]
        ENR["enrollment"]
    end

    subgraph Operations ["Academic Operations"]
        TEACH["teaching"]
        ATT["attendance"]
        ASS["assessment"]
        CERT["certification"]
        RULE["rule-engine"]
    end

    %% Boot depends on all
    BOOT --> ID & ORG & CFG & PEOPLE & CUR & ACAD & ENR & TEACH & ATT & ASS & CERT & RULE

    %% All depend on shared-kernel
    ID & ORG & CFG & PEOPLE & CUR & ACAD & ENR & TEACH & ATT & ASS & CERT & RULE --> SHARED

    %% Inter-module dependencies (Strict DAG)
    ORG -.->|Port: OrganizationAccountQuery| ID
    PEOPLE --> ORG
    CUR --> ORG
    ACAD --> ORG
    CFG --> ORG

    ENR --> PEOPLE
    ENR --> CUR
    ENR --> ACAD
    ENR -.->|Thẩm định điều kiện| RULE

    TEACH --> PEOPLE
    TEACH --> ACAD

    ATT --> ACAD
    ATT --> PEOPLE

    ASS --> ACAD
    ASS --> PEOPLE
    ASS -.->|Thẩm định thang điểm| RULE

    CERT --> PEOPLE
    CERT --> CUR
    CERT -.->|Thẩm định tốt nghiệp| RULE
```

---

## 2. Các Quy Tắc Phụ Thuộc Bất Biến

1. **Tuyệt đối không có phụ thuộc vòng (No Cyclic Dependencies)**:
   - Module A phụ thuộc Module B thì Module B **nghiêm cấm** phụ thuộc trực tiếp hoặc gián tiếp ngược lại Module A.
   - Nếu phát hiện nhu cầu giao tiếp 2 chiều: phải chuyển sang sử dụng **Domain Events** (bất đồng bộ) hoặc tách một abstraction dùng chung vào `shared-kernel` hoặc sử dụng **Inversion of Control (Public Ports / SPI)**.

2. **Quy tắc về `shared-kernel`**:
   - `shared-kernel` chỉ chứa các giá trị định danh bất biến (Strongly Typed IDs: `UserId`, `OrganizationId`), Base Entities (`AuditableEntity`), Domain Event contracts (`DomainEvent`), và DTO lỗi (`ApiErrorResponse`).
   - `shared-kernel` **tuyệt đối không được phụ thuộc vào bất kỳ module nghiệp vụ nào**.

3. **Quy tắc về `boot` (Composition Root)**:
   - Module `boot` là nơi chứa hàm `main()`, cấu hình Spring Security Filter Chain, Flyway migrations tập hợp và khởi tạo Spring Context.
   - `boot` phụ thuộc vào tất cả các module nghiệp vụ. Các module nghiệp vụ **tuyệt đối không được phụ thuộc vào `boot`**.

4. **Quy tắc Package Visibility**:
   - Chỉ các class/interface nằm trong package `api` của mỗi module mới có phạm vi `public`.
   - Toàn bộ các class trong package `application`, `domain`, `infrastructure` phải là `package-private` hoặc không được expose ra ngoài Maven module boundary.
