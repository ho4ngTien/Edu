# Checkpoint 2.7.5 — Method-Level Security Enforcement

> **Thuộc Phase**: [Phase 2.7 — Authorization & Access Control](README.md)  
> **Trạng thái**: 📐 Đã đặc tả kỹ thuật (AOP Security Approved)  
> **Tiêu chí nghiệm thu**: `AC-2.7.5`

---

## 1. Mục tiêu Checkpoint

Chuẩn hóa việc kiểm soát quyền truy cập tại tầng nghiệp vụ (Application / Service Layer) bằng Spring Security AOP Interceptors. Đảm bảo mọi phương thức nhạy cảm đều được bảo vệ độc lập, không phụ thuộc vào việc Controller có được cấu hình đúng hay không, và không bị bỏ qua khi được gọi từ các luồng nội bộ (event listeners, asynchronous jobs, internal adapters).

---

## 2. Nguyên tắc Thiết kế & Triển khai

1. **Kích hoạt Method Security chuẩn**: Bật `@EnableMethodSecurity(prePostEnabled = true)` trong lớp cấu hình `SecurityConfig`.
2. **Ưu tiên `@PreAuthorize`**: Dùng `@PreAuthorize("hasAuthority('PERMISSION_CODE')")` để kiểm tra quyền hạn trước khi thân phương thức được thực thi. Nếu người dùng thiếu quyền, Spring Security ném `AccessDeniedException` ngay lập tức.
3. **Tránh biểu thức SpEL phức tạp**: Không đưa toàn bộ logic nghiệp vụ (như kiểm tra điều kiện điểm số, xác minh trạng thái nộp học phí) vào biểu thức SpEL của `@PreAuthorize`. SpEL chỉ nên dùng để kiểm tra quyền hạn nguyên tử (`hasAuthority`) hoặc kiểm tra quyền sở hữu ID cơ bản (`#id == authentication.principal.id`).
4. **Phòng tránh lỗi Self-Invocation**: Spring Security sử dụng Dynamic Proxies (CGLIB/JDK). Nếu một phương thức trong cùng class gọi một phương thức khác có gắn `@PreAuthorize`, lời gọi sẽ đi qua `this` thay vì qua proxy, dẫn đến việc bỏ qua hoàn toàn bước kiểm tra quyền! Mọi phương thức cần bảo vệ độc lập phải được gọi từ bean khác hoặc được tách riêng biệt.
5. **Defense-in-Depth**: `@PreAuthorize` là chốt chặn vòng ngoài tại tầng service; bên trong phương thức vẫn cần gọi `ResourceAuthorizationService` để kiểm tra phạm vi tài nguyên cụ thể.

---

## 3. Mẫu Thiết kế Chuẩn (Reference Implementation)

Ví dụ dưới đây minh họa chuẩn triển khai bảo vệ phương thức trong module quản lý khóa học (`curriculum`):

```java
package com.edu.curriculum.application.service;

import com.edu.curriculum.api.dto.CourseResponse;
import com.edu.curriculum.api.dto.UpdateCourseRequest;
import com.edu.curriculum.domain.model.Course;
import com.edu.curriculum.domain.repository.CourseRepository;
import com.edu.shared.kernel.domain.tenant.TenantContext;
import com.edu.shared.kernel.exception.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CourseApplicationService {

    private final CourseRepository courseRepository;
    private final CourseAuthorizationService courseAuthorizationService;

    public CourseApplicationService(
            CourseRepository courseRepository,
            CourseAuthorizationService courseAuthorizationService
    ) {
        this.courseRepository = courseRepository;
        this.courseAuthorizationService = courseAuthorizationService;
    }

    /**
     * Đọc thông tin khóa học: Yêu cầu quyền CURRICULUM_COURSE_READ.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('CURRICULUM_COURSE_READ')")
    public CourseResponse getCourse(UUID courseId) {
        var tenantInfo = TenantContext.require();

        // 1. Lọc theo tenant ngay từ truy vấn database
        Course course = courseRepository.findByIdAndOrganizationId(courseId, tenantInfo.organizationId().value())
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        // 2. Thẩm định quyền trên tài nguyên chi tiết (Resource-Level Authorization)
        courseAuthorizationService.checkCanRead(course, tenantInfo);

        return CourseResponse.from(course);
    }

    /**
     * Cập nhật khóa học: Yêu cầu quyền CURRICULUM_COURSE_UPDATE.
     */
    @PreAuthorize("hasAuthority('CURRICULUM_COURSE_UPDATE')")
    public CourseResponse updateCourse(UUID courseId, UpdateCourseRequest request) {
        var tenantInfo = TenantContext.require();

        Course course = courseRepository.findByIdAndOrganizationId(courseId, tenantInfo.organizationId().value())
                .orElseThrow(() -> new ResourceNotFoundException("Course", courseId));

        courseAuthorizationService.checkCanModify(course, tenantInfo);

        course.updateDetails(request.title(), request.description(), request.creditHours());
        return CourseResponse.from(course);
    }
}
```

---

## 4. Ma trận Phân tách Trách nhiệm Kiểm tra Quyền

| Tầng kiểm soát | Công nghệ áp dụng | Trách nhiệm cụ thể |
| :--- | :--- | :--- |
| **Security Filter** | `TenantResolutionFilter` | Đảm bảo request có token hợp lệ, nạp đúng Authorities theo `X-Organization-Id`. |
| **Method Security** | `@PreAuthorize("hasAuthority('...')")` | Chặn đứng người dùng không có quyền tương ứng trước khi chạm vào logic service. |
| **Service Layer** | Application Service | Lấy thông tin `TenantContext.require()`, nạp thực thể theo tenant scope. |
| **Resource Policy** | `ResourceAuthorizationService` | Kiểm tra quyền sâu: Giảng viên phụ trách, trạng thái khóa học đã xuất bản hay chưa. |

---

## 5. Tiêu chí Đánh giá & Nghiệm thu (Checklist AC-2.7.5)

- [x] **AC-2.7.5.1**: Cấu hình `@EnableMethodSecurity` được kích hoạt và kiểm thử thành công trong `SecurityConfig`.
- [x] **AC-2.7.5.2**: Toàn bộ các Application Services chứa thao tác nhạy cảm đều được bảo vệ bằng `@PreAuthorize` với mã quyền chuẩn hóa.
- [x] **AC-2.7.5.3**: Kiểm tra phạm vi tài nguyên (Resource-scope check) được gọi tường minh bên trong service, không phó mặc hoàn toàn cho SpEL.
- [x] **AC-2.7.5.4**: Không có đường gọi nghiệp vụ quan trọng nào bị bypass do lỗi self-invocation của Spring proxy.
- [x] **AC-2.7.5.5**: Ranh giới module được bảo vệ nghiêm ngặt; mỗi module tự sở hữu service kiểm tra quyền tài nguyên của chính mình.
- [x] **AC-2.7.5.6**: Kiểm thử xác nhận người dùng có quyền thì thực thi thành công, thiếu quyền bị từ chối với `AccessDeniedException`.
- [x] **AC-2.7.5.7**: Mọi `AccessDeniedException` phát sinh tại tầng service đều được `GlobalExceptionHandler` bắt và chuyển đổi về `ApiErrorResponse` (HTTP 403).
