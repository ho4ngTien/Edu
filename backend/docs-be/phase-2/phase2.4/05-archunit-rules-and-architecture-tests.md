# Phase 2.4 — Thành phần 05: ArchUnit Rules & Automated Architecture Tests

Tài liệu này đặc tả 9 quy tắc kiểm tra kiến trúc tự động được hiện thực hóa bằng thư viện ArchUnit trong module `tests/architecture-tests`.

---

## 1. Danh Sách 9 Quy Tắc Kiểm Tra Kiến Trúc

| Quy tắc | Tên Test Method | Mô tả quy tắc kỹ thuật |
| :---: | :--- | :--- |
| **Rule 1** | `sharedKernelShouldNotDependOnModules` | Package `com.edu.shared.kernel..` **tuyệt đối không được phụ thuộc** vào bất kỳ class nào thuộc `com.edu.modules..`. |
| **Rule 2** | `noCyclicDependenciesBetweenModules` | Đồ thị phụ thuộc giữa các package của các module phải là một đồ thị có hướng không chu trình (DAG); **cấm cyclic dependencies**. |
| **Rule 3** | `bootShouldNotBeDependedOnByModules` | Không một module nghiệp vụ nào (`com.edu.modules..`) hoặc `shared-kernel` được phép import class của `com.edu.boot..`. |
| **Rule 4** | `domainShouldNotDependOnInfrastructure` | Trong từng module, package `domain..` không được phép phụ thuộc vào package `infrastructure..`. |
| **Rule 5** | `entitiesShouldResideInPersistencePackage` | Mọi class có gắn annotation `@Entity` bắt buộc phải nằm trong package `..infrastructure.persistence.entity..`. |
| **Rule 6** | `repositoriesShouldResideInPersistencePackage` | Mọi class/interface kế thừa `JpaRepository` bắt buộc phải nằm trong package `..infrastructure.persistence.repository..`. |
| **Rule 7** | `controllersShouldResideInWebPackage` | Mọi class có gắn annotation `@RestController` bắt buộc phải nằm trong package `..infrastructure.web..` và tên kết thúc bằng `Controller`. |
| **Rule 8** | `noDirectCrossModuleEntityAccess` | Module A không được phép import hoặc truy cập trực tiếp JPA Entity của Module B. |
| **Rule 9** | `auditableEntitiesShouldExtendAuditableEntity` | Các JPA Entities nghiệp vụ trong các module phải kế thừa từ lớp cha `AuditableEntity`. |

---

## 2. Mã Triển Khai Kiểm Thử ArchUnit Mẫu

Tệp đặt tại: `tests/architecture-tests/src/test/java/com/edu/tests/architecture/ModuleBoundaryTest.java`

```java
@AnalyzeClasses(packages = "com.edu", importOptions = ImportOption.DoNotIncludeTests.class)
public class ModuleBoundaryTest {

    @ArchTest
    static final ArchRule sharedKernelShouldNotDependOnModules =
        noClasses().that().resideInAPackage("com.edu.shared.kernel..")
            .should().dependOnClassesThat().resideInAPackage("com.edu.modules..");

    @ArchTest
    static final ArchRule noCyclicDependencies =
        slices().matching("com.edu.modules.(*)..")
            .should().beFreeOfCycles();
}
```
