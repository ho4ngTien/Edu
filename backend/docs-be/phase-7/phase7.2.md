# Phase 7.2 — Architecture Tests với ArchUnit (9 Rules)

Tài liệu đặc tả 9 quy tắc kiến trúc tự động hóa bằng **ArchUnit 1.4.1** trong module `tests/architecture-tests`, kiểm soát ranh giới phụ thuộc và tính toàn vẹn cấu trúc toàn bộ backend E-Edu.

---

## 1. Mục tiêu & Nguyên lý

**Architecture Tests** là lớp bảo vệ tự động (automated guardrail) chạy trong mỗi lần build Maven, đảm bảo:
- Không có module nào vi phạm ranh giới phụ thuộc đã được quyết định trong Phase 1.
- Không có chu trình phụ thuộc vòng (cyclic dependencies) giữa các module.
- Dữ liệu nhạy cảm (như `passwordHash`) không bao giờ bị lộ ra tầng API.
- Shared Kernel không phụ thuộc vào module nghiệp vụ cụ thể nào.

**Khi nào Architecture Tests fail?** → Khi developer vô tình vi phạm quy tắc (ví dụ: import trực tiếp `UserEntity` từ module `organization`). Build Maven sẽ fail ngay lập tức, trước khi code được merge vào `main`.

---

## 2. Cấu trúc Module Architecture Tests

```text
backend/tests/architecture-tests/
├── pom.xml
└── src/test/java/com/edu/architecture/
    ├── ArchitectureTestsSmokeTest.java          ← Smoke test (JUnit framework loaded)
    ├── SharedKernelArchitectureTest.java         ← AR-01, AR-05
    ├── ModuleDependencyArchitectureTest.java     ← AR-02
    └── IdentityOrganizationArchitectureTest.java ← AR-07, AR-08, AR-09
```

---

## 3. Danh sách 9 Quy tắc Kiến trúc

| Mã Rule | Tên | Test Class | Mô tả |
| :---: | :--- | :--- | :--- |
| **AR-01** | Shared Kernel Isolation | `SharedKernelArchitectureTest` | `com.edu.shared` không phụ thuộc vào bất kỳ module nghiệp vụ nào |
| **AR-02** | No Cyclic Dependencies | `ModuleDependencyArchitectureTest` | Không có chu trình phụ thuộc vòng giữa các module `com.edu.*` |
| **AR-03** | *(Kế hoạch)* Controller không chứa Business Logic | *(Kế hoạch)* | Controller chỉ xử lý HTTP binding, không chứa `@Transactional` trực tiếp |
| **AR-04** | *(Kế hoạch)* Repository không bị gọi trực tiếp từ Controller | *(Kế hoạch)* | Controller không import class từ `infrastructure.persistence.repository` |
| **AR-05** | Domain Events không phụ thuộc JPA | `SharedKernelArchitectureTest` | Lớp implement `DomainEvent` không import `jakarta.persistence` |
| **AR-06** | *(Kế hoạch)* Service không phụ thuộc Web Layer | *(Kế hoạch)* | Service không import `HttpServletRequest`, `@RestController` |
| **AR-07** | No Cross-Schema Direct Entity Access | `IdentityOrganizationArchitectureTest` | `organization` không phụ thuộc `identity.infrastructure.persistence` |
| **AR-08** | Public Port Dependency Only | `IdentityOrganizationArchitectureTest` | `organization` không phụ thuộc bất kỳ class nào của `identity` |
| **AR-09** | No Password Leakage in DTOs | `IdentityOrganizationArchitectureTest` | DTO trong `identity.api.dto` không có field `passwordHash` hay `rawPassword` |

---

## 4. Chi tiết Mã nguồn Từng Rule

### AR-01: Shared Kernel Không Phụ thuộc Business Modules

```java
// SharedKernelArchitectureTest.java
@Test
@DisplayName("AR-01: Shared Kernel khong duoc phu thuoc vao cac Business Modules")
void sharedKernelMustNotDependOnBusinessModules() {
    ArchRule rule = noClasses()
            .that().resideInAPackage("com.edu.shared..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "com.edu.identity..",
                    "com.edu.organization..",
                    "com.edu.people..",
                    "com.edu.curriculum..",
                    "com.edu.academic..",
                    "com.edu.enrollment..",
                    "com.edu.teaching..",
                    "com.edu.attendance..",
                    "com.edu.assessment..",
                    "com.edu.ruleengine..",
                    "com.edu.configuration..",
                    "com.edu.certification.."
            )
            .allowEmptyShould(true);

    rule.check(importedClasses);
}
```

### AR-02: Không Có Cyclic Dependencies

```java
// ModuleDependencyArchitectureTest.java
@Test
@DisplayName("AR-02: Khong duoc co chu trinh phu thuoc vong (cycles) giua cac modules")
void modulesMustBeFreeOfCycles() {
    slices()
            .matching("com.edu.(*)..")
            .should()
            .beFreeOfCycles()
            .check(importedClasses);
}
```

### AR-05: Domain Events Không Phụ thuộc JPA

```java
// SharedKernelArchitectureTest.java
@Test
@DisplayName("AR-05: Domain Events khong duoc phu thuoc vao JPA Entities")
void domainEventsMustNotDependOnJpa() {
    ArchRule rule = noClasses()
            .that().implement("com.edu.shared.kernel.domain.event.DomainEvent")
            .should().dependOnClassesThat()
            .resideInAPackage("jakarta.persistence..")
            .allowEmptyShould(true);

    rule.check(importedClasses);
}
```

### AR-07: No Cross-Schema Direct Entity Access

```java
// IdentityOrganizationArchitectureTest.java
@Test
@DisplayName("AR-07: NoCrossSchemaDirectEntityAccess - organization must not depend on identity persistence")
void organizationMustNotDependOnIdentityPersistence() {
    noClasses()
            .that().resideInAPackage("com.edu.organization..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "com.edu.identity.infrastructure.persistence.entity..",
                    "com.edu.identity.infrastructure.persistence.repository.."
            )
            .allowEmptyShould(true)
            .check(importedClasses);
}
```

### AR-08: Public Port Dependency Only

```java
@Test
@DisplayName("AR-08: PublicPortDependencyOnly - organization must not depend on identity internal implementation")
void organizationMustNotDependOnIdentity() {
    noClasses()
            .that().resideInAPackage("com.edu.organization..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.edu.identity..")
            .allowEmptyShould(true)
            .check(importedClasses);
}
```

### AR-09: No Password Leakage in DTOs

```java
@Test
@DisplayName("AR-09: NoPasswordLeakageInDTOs - identity DTOs must not contain passwordHash or rawPassword fields")
void identityDtosMustNotLeakPasswordFields() {
    fields()
            .that().areDeclaredInClassesThat().resideInAPackage("com.edu.identity.api.dto..")
            .should().notHaveName("passwordHash")
            .andShould().notHaveName("rawPassword")
            .allowEmptyShould(true)
            .check(importedClasses);
}
```

---

## 5. Cấu hình Maven (`tests/architecture-tests/pom.xml`)

```xml
<dependencies>
    <dependency>
        <groupId>com.edu</groupId>
        <artifactId>identity</artifactId>
        <version>${project.version}</version>
    </dependency>
    <dependency>
        <groupId>com.edu</groupId>
        <artifactId>organization</artifactId>
        <version>${project.version}</version>
    </dependency>
    <dependency>
        <groupId>com.edu</groupId>
        <artifactId>shared-kernel</artifactId>
        <version>${project.version}</version>
    </dependency>
    <dependency>
        <groupId>com.tngtech.archunit</groupId>
        <artifactId>archunit-junit5</artifactId>
        <version>1.4.1</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

---

## 6. Tích hợp vào CI/CD Pipeline

Architecture tests chạy tự động trong mỗi pull request qua GitHub Actions. Nếu bất kỳ rule nào fail, build sẽ dừng và PR không thể merge.

```yaml
# .github/workflows/ci.yml
- name: Run Architecture Tests
  run: mvn -pl tests/architecture-tests test -am
```

---

## 7. Liên kết Tài liệu Liên quan

- [Phase 7 README](README.md) — Tổng quan phân hệ Quality & Delivery
- [Phase 7.1: Unit Tests & Slice Tests](phase7.1.md) — Kiểm thử đơn vị
- [Phase 4.3: Public Port Decoupling](../phase-4/phase4.3.md) — Lý do AR-07 và AR-08 tồn tại

