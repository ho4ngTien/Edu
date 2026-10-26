# Phase 2.1 — Thành phần 01: Maven Multi-Module Hierarchy

Tài liệu này đặc tả cấu trúc cây dự án Maven Multi-Module (Reactor) và phân cấp các module trong E-Edu.

---

## 1. Cấu Trúc Phân Cấp 19 Modules Trong Reactor

```text
com.edu:parent (backend/pom.xml)
│
├── com.edu:boot (boot/pom.xml) [jar]
│
├── com.edu:shared (shared/pom.xml) [pom]
│   └── com.edu:shared-kernel (shared/shared-kernel/pom.xml) [jar]
│
├── com.edu:modules (modules/pom.xml) [pom]
│   ├── com.edu:identity (modules/identity/pom.xml) [jar]
│   ├── com.edu:organization (modules/organization/pom.xml) [jar]
│   ├── com.edu:configuration (modules/configuration/pom.xml) [jar]
│   ├── com.edu:people (modules/people/pom.xml) [jar]
│   ├── com.edu:curriculum (modules/curriculum/pom.xml) [jar]
│   ├── com.edu:academic (modules/academic/pom.xml) [jar]
│   ├── com.edu:enrollment (modules/enrollment/pom.xml) [jar]
│   ├── com.edu:teaching (modules/teaching/pom.xml) [jar]
│   ├── com.edu:attendance (modules/attendance/pom.xml) [jar]
│   ├── com.edu:assessment (modules/assessment/pom.xml) [jar]
│   ├── com.edu:certification (modules/certification/pom.xml) [jar]
│   └── com.edu:rule-engine (modules/rule-engine/pom.xml) [jar]
│
└── com.edu:tests (tests/pom.xml) [pom]
    └── com.edu:architecture-tests (tests/architecture-tests/pom.xml) [jar]
```

---

## 2. Nguyên Tắc Phân Loại Packaging

1. **Packaging `pom` (Parent & Aggregator)**:
   - Bao gồm: Root `pom.xml`, `shared/pom.xml`, `modules/pom.xml`, `tests/pom.xml`.
   - Không chứa mã nguồn Java trong thư mục `src/`.
   - Chứa thẻ `<modules>` để điều phối thứ tự build Maven Reactor.
2. **Packaging `jar` (Leaf Modules)**:
   - Bao gồm 11 module nghiệp vụ, `shared-kernel` và `architecture-tests`.
   - Chứa mã nguồn thực tế và unit test của từng phân hệ.
3. **Packaging `jar` thực thi (Boot Application)**:
   - Module `boot`: Đóng gói thành Fat JAR chứa toàn bộ class và dependencies bên trong, sẵn sàng chạy với lệnh `java -jar`.
