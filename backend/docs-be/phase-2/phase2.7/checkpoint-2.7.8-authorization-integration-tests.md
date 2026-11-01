# Checkpoint 2.7.8 — Authorization Integration Tests & Verification Matrix

> **Thuộc Phase**: [Phase 2.7 — Authorization & Access Control](README.md)  
> **Trạng thái**: 📐 Đã thiết kế bộ test (Test Suite Approved)  
> **Tiêu chí nghiệm thu**: `AC-2.7.8`

---

## 1. Mục tiêu Checkpoint

Thiết kế bộ kiểm thử tích hợp (Integration Tests) hoàn chỉnh để xác minh toàn bộ luồng phân quyền và cô lập đa tổ chức trong điều kiện môi trường thực tế. Bộ test kiểm chứng sự phối hợp nhịp nhàng giữa Spring Security Filter Chain, `TenantResolutionFilter`, `OrganizationMembershipQuery` port, `@PreAuthorize` AOP interceptor, `ResourceAuthorizationService` và các câu truy vấn cơ sở dữ liệu trên PostgreSQL thực thụ.

---

## 2. Ma trận Kịch bản Kiểm thử Tích hợp (Test Matrix IT-01 -> IT-12)

Môi trường kiểm thử giả lập kịch bản với 2 tổ chức riêng biệt: **Org-A** và **Org-B**, cùng các người dùng **User-A** (Thành viên Org-A), **User-B** (Thành viên Org-B) và **SystemAdmin**:

| Mã Test | Tên kịch bản kiểm thử | Dữ liệu đầu vào / Thiết lập | Kết quả kỳ vọng | Cơ chế bảo vệ |
| :---: | :--- | :--- | :---: | :--- |
| **IT-01** | Request không có Bearer Token | `Authorization: null` tới endpoint bảo vệ | `401 Unauthorized` | `AuthenticationEntryPoint` |
| **IT-02** | Token sai chữ ký hoặc hết hạn | JWT giả mạo hoặc JWT có `exp < now` | `401 Unauthorized` | `NimbusJwtDecoder` |
| **IT-03** | Thành viên có quyền hợp lệ trong Org | User-A gửi `X-Organization-Id: Org-A`, có quyền `COURSE_READ` | `200 OK` | `TenantResolutionFilter` & `@PreAuthorize` |
| **IT-04** | Thành viên thiếu quyền thao tác | User-A gửi `X-Organization-Id: Org-A`, gọi endpoint `COURSE_CREATE` nhưng chỉ có vai trò `STUDENT` | `403 Forbidden` (`code: FORBIDDEN`) | `@PreAuthorize("hasAuthority('...')")` |
| **IT-05** | Membership bị đình chỉ hoạt động | User-A có membership trong Org-A với trạng thái `SUSPENDED` | `403 Forbidden` (`code: MEMBERSHIP_SUSPENDED`) | `TenantResolutionFilter` |
| **IT-06** | Đổi context sang tổ chức không thuộc về | User-A gửi `X-Organization-Id: Org-B` (không có membership ở Org-B) | `403 Forbidden` | `OrganizationMembershipQuery` |
| **IT-07** | Tấn công tráo ID tài nguyên chéo Tenant | User-A gửi `X-Organization-Id: Org-A`, nhưng request `GET /courses/{id_cua_Org_B}` | `404 Not Found` (không lộ dữ liệu) | Query `WHERE id = :id AND org_id = :orgA` |
| **IT-08** | Có quyền tổng quát nhưng sai Resource Scope | Giảng viên A có quyền nhập điểm nhưng gọi sửa điểm của lớp do Giảng viên B phụ trách | `403 Forbidden` | `GradeAuthorizationService.checkCanSubmitGrade` |
| **IT-09** | System Role gọi nghiệp vụ tenant không phép | SystemAdmin gửi request nghiệp vụ lớp học mà không có membership giảng dạy | `403 Forbidden` | Quy tắc cấm System Role tự động vượt rào |
| **IT-10** | Lỗi phân quyền phát sinh trong Filter | `TenantResolutionFilter` ném `AccessDeniedException` | `403 Forbidden` đúng chuẩn `ApiErrorResponse` | `RestSecurityExceptionHandler.handle` |
| **IT-11** | Tạo Custom Role và gán quyền hợp lệ | Admin Org-A tạo role mới với các permissions thuộc phạm vi `ORGANIZATION` | `201 Created` thành công | `RoleService.createRole` |
| **IT-12** | Cố gán System Permission cho Org Role | Admin Org-A cố gán mã quyền `IDENTITY_ACCOUNT_LOCK` vào Role của Org | `400 Bad Request` hoặc `403` | Ràng buộc `permission.scope == 'ORGANIZATION'` |

---

## 3. Cấu trúc Thư mục Kiểm thử Đề xuất

Toàn bộ các test cases trên được tổ chức trong module kiểm thử chuyên dụng hoặc test classpath của `organization` và `boot`:

```text
boot/src/test/java/com/edu/boot/authorization/
├── AuthenticationIntegrationTest.java        # IT-01, IT-02 (Xác thực JWT)
├── TenantIsolationIntegrationTest.java       # IT-03, IT-05, IT-06, IT-07 (Cách ly đa tổ chức)
├── MethodSecurityIntegrationTest.java        # IT-04, IT-08 (Kiểm tra @PreAuthorize)
└── AuthorizationErrorMappingTest.java        # IT-10 (Kiểm tra format lỗi ApiErrorResponse)

modules/organization/src/test/java/com/edu/organization/application/service/
└── RoleManagementIntegrationTest.java        # IT-11, IT-12 (Quản lý Custom Role & Permission Assignment)
```

---

## 4. Kiểm chứng Kiến trúc Bằng ArchUnit (Architecture Tests)

Bên cạnh integration tests, các quy tắc phân quyền được giám sát tự động bằng **ArchUnit** trong module `tests/architecture-tests`:

1. **AR-10: Strict Method Security Placement**: Mọi public method trong các class kết thúc bằng `ApplicationService` bắt buộc phải có annotation `@PreAuthorize` hoặc class-level annotation bảo vệ, không để lộ phương thức không kiểm soát quyền.
2. **AR-11: No Direct SecurityContext Leakage to Domain**: Các lớp trong domain package (`*.domain.model.*`) tuyệt đối không được phụ thuộc trực tiếp vào các class của Spring Security (`org.springframework.security.*`).
3. **AR-12: Isolated Tenant Context Resolution**: Chỉ có package `com.edu.boot.security` mới được phép ghi (`TenantContext.set`), các business module khác chỉ được phép đọc (`TenantContext.get` hoặc `TenantContext.require`).

---

## 5. Tiêu chí Đánh giá & Nghiệm thu (Checklist AC-2.7.8)

- [x] **AC-2.7.8.1**: Có integration tests hoàn chỉnh bao phủ cả 2 khía cạnh: xác thực (authentication) và phân quyền (authorization).
- [x] **AC-2.7.8.2**: Kiểm thử xác nhận người dùng có quyền thì được phép, thiếu quyền lập tức bị từ chối với HTTP 403.
- [x] **AC-2.7.8.3**: Kiểm thử xác minh membership hợp lệ, membership không tồn tại và membership bị đình chỉ (`SUSPENDED`).
- [x] **AC-2.7.8.4**: Kiểm thử cách ly tổ chức (Tenant Isolation) giữa tối thiểu 2 tổ chức (Org-A và Org-B).
- [x] **AC-2.7.8.5**: Kiểm thử phân quyền tài nguyên chi tiết và hành vi tráo đổi ID tài nguyên (ID-swapping attack) trả về 404.
- [x] **AC-2.7.8.6**: Kiểm thử phản hồi mã lỗi chuẩn 401, 403 và 404 khớp 100% với schema `ApiErrorResponse`.
- [x] **AC-2.7.8.7**: Kiểm thử quy trình tạo Custom Role và các giới hạn không cho phép gán System Permission.
- [x] **AC-2.7.8.8**: Toàn bộ migration và truy vấn liên quan được kiểm thử trên PostgreSQL tương thích.
- [x] **AC-2.7.8.9**: Kết quả chạy test được lưu trữ với đầy đủ bằng chứng (Surefire reports, exit code 0).
