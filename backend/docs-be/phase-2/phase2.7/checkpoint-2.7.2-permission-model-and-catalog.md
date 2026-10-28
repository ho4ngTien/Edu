# Checkpoint 2.7.2 — Permission Model & Canonical Catalog

> **Thuộc Phase**: [Phase 2.7 — Authorization & Access Control](README.md)  
> **Trạng thái**: 📐 Đã đặc tả chuẩn hóa (Catalog Approved)  
> **Tiêu chí nghiệm thu**: `AC-2.7.2`

---

## 1. Mục tiêu Checkpoint

Thiết lập một danh mục quyền chuẩn hóa (Canonical Permission Catalog) cho toàn bộ hệ thống E-Edu, đặt nền móng cho việc phân quyền nguyên tử (Fine-Grained RBAC). Chuẩn hóa quy tắc đặt tên, phạm vi áp dụng (Scope), tính bất biến của mã quyền và cơ chế kiểm tra tính hợp lệ trước khi gán cho Role.

---

## 2. Quy ước Đặt tên Quyền (Naming Convention)

Tất cả các mã quyền trong E-Edu bắt buộc tuân thủ định dạng cú pháp 3 phần (Tri-part syntax), viết hoa toàn bộ và ngăn cách bằng dấu gạch dưới (`_`):

$$\mathbf{DOMAIN\_RESOURCE\_ACTION}$$

Trong đó:
1. **DOMAIN**: Tên phân hệ nghiệp vụ sở hữu quyền (ví dụ: `ORG`, `IDENTITY`, `CURRICULUM`, `ACADEMIC`, `ASSESSMENT`, `PEOPLE`, `CONFIG`).
2. **RESOURCE**: Tên thực thể hoặc đối tượng bị tác động (ví dụ: `MEMBER`, `ROLE`, `COURSE`, `SECTION`, `GRADE`, `STUDENT`).
3. **ACTION**: Hành động cụ thể được phép thực thi (ví dụ: `READ`, `CREATE`, `UPDATE`, `DELETE`, `MANAGE`, `SUBMIT`, `APPROVE`, `FINALIZE`).

*Ví dụ chuẩn*:
- `ORG_MEMBER_INVITE`: Quyền mời thành viên mới vào tổ chức.
- `CURRICULUM_COURSE_CREATE`: Quyền tạo mới chương trình đào tạo hoặc khóa học.
- `ASSESSMENT_GRADE_SUBMIT`: Quyền nhập và nộp điểm thành phần cho học sinh.
- `ASSESSMENT_GRADE_FINALIZE`: Quyền phê duyệt và khóa sổ điểm học kỳ.

---

## 3. Phân định Phạm vi Quyền (Permission Scope)

E-Edu chia Permission thành 2 phạm vi áp dụng riêng biệt:

1. **Organization-Level Permission (`scope = 'ORGANIZATION'`)**:
   - Áp dụng trong phạm vi của một tổ chức/trường học cụ thể.
   - Chỉ được gán cho các vai trò thuộc tổ chức (Organization Roles).
   - Yêu cầu phải có `TenantContext` hợp lệ khi kiểm tra quyền.
2. **System-Level Permission (`scope = 'SYSTEM'`)**:
   - Áp dụng trên quy mô toàn nền tảng.
   - Chỉ được gán cho System Administrator.
   - Được phép thao tác trên các endpoint quản trị hạ tầng, cấp phát tổ chức mới, hoặc giám sát vi phạm toàn hệ thống.

---

## 4. Danh mục Quyền Chuẩn hóa Toàn Hệ thống (Master Catalog)

Dưới đây là danh mục quyền tiêu chuẩn được khởi tạo sẵn (seed) vào hệ thống:

### 4.1. Phân hệ Quản trị Tổ chức & Phân quyền (`organization`)

| Mã quyền | Phạm vi | Mô tả chi tiết |
| :--- | :---: | :--- |
| `ORG_PROFILE_READ` | ORGANIZATION | Xem thông tin chi tiết cấu hình và hồ sơ của tổ chức. |
| `ORG_PROFILE_UPDATE` | ORGANIZATION | Chỉnh sửa tên, logo, địa chỉ và thông tin liên hệ của tổ chức. |
| `ORG_MEMBER_READ` | ORGANIZATION | Xem danh sách thành viên và trạng thái tham gia của tổ chức. |
| `ORG_MEMBER_INVITE` | ORGANIZATION | Mời người dùng tham gia vào tổ chức qua email. |
| `ORG_MEMBER_MANAGE` | ORGANIZATION | Thay đổi vai trò, đình chỉ (`SUSPEND`) hoặc xóa thành viên khỏi tổ chức. |
| `ORG_ROLE_READ` | ORGANIZATION | Xem danh sách các Role và Permissions được cấu hình trong tổ chức. |
| `ORG_ROLE_MANAGE` | ORGANIZATION | Tạo mới, chỉnh sửa hoặc xóa Custom Roles trong tổ chức. |

### 4.2. Phân hệ Danh tính & Tài khoản (`identity`)

| Mã quyền | Phạm vi | Mô tả chi tiết |
| :--- | :---: | :--- |
| `IDENTITY_ACCOUNT_READ` | SYSTEM | Xem thông tin tài khoản người dùng trên toàn hệ sinh thái. |
| `IDENTITY_ACCOUNT_LOCK` | SYSTEM | Khóa hoặc vô hiệu hóa tài khoản vi phạm chính sách bảo mật nền tảng. |
| `IDENTITY_TOKEN_REVOKE` | SYSTEM | Thu hồi cưỡng chế chuỗi refresh token của một tài khoản cụ thể. |

### 4.3. Phân hệ Hồ sơ Con người (`people`)

| Mã quyền | Phạm vi | Mô tả chi tiết |
| :--- | :---: | :--- |
| `PEOPLE_STUDENT_READ` | ORGANIZATION | Xem thông tin hồ sơ học sinh, sinh viên trong tổ chức. |
| `PEOPLE_STUDENT_MANAGE` | ORGANIZATION | Tiếp nhận, tạo mới và cập nhật hồ sơ cá nhân học sinh. |
| `PEOPLE_TEACHER_READ` | ORGANIZATION | Xem danh sách và hồ sơ giảng viên, cán bộ giáo dục. |
| `PEOPLE_TEACHER_MANAGE` | ORGANIZATION | Quản lý thông tin hợp đồng, phân loại ngạch giảng viên. |

### 4.4. Phân hệ Chương trình Đào tạo (`curriculum`)

| Mã quyền | Phạm vi | Mô tả chi tiết |
| :--- | :---: | :--- |
| `CURRICULUM_COURSE_READ` | ORGANIZATION | Xem danh mục khóa học, môn học và chương trình đào tạo. |
| `CURRICULUM_COURSE_CREATE` | ORGANIZATION | Tạo mới khóa học và môn học vào danh mục tổ chức. |
| `CURRICULUM_COURSE_UPDATE` | ORGANIZATION | Chỉnh sửa cấu trúc khóa học, đề cương chi tiết (syllabus). |
| `CURRICULUM_COURSE_DELETE` | ORGANIZATION | Vô hiệu hóa hoặc xóa khóa học khỏi chương trình. |

### 4.5. Phân hệ Kế hoạch & Lớp học (`academic`)

| Mã quyền | Phạm vi | Mô tả chi tiết |
| :--- | :---: | :--- |
| `ACADEMIC_PERIOD_MANAGE` | ORGANIZATION | Quản lý niên khóa, học kỳ, khung thời gian học tập. |
| `ACADEMIC_SECTION_READ` | ORGANIZATION | Xem danh sách lớp học phần (class section) và thời khóa biểu. |
| `ACADEMIC_SECTION_MANAGE` | ORGANIZATION | Mở lớp học phần, chỉ định giảng viên và xếp lịch học. |

### 4.6. Phân hệ Ghi danh & Tuyển sinh (`enrollment`)

| Mã quyền | Phạm vi | Mô tả chi tiết |
| :--- | :---: | :--- |
| `ENROLLMENT_RECORD_READ` | ORGANIZATION | Xem danh sách học viên đăng ký vào các lớp học phần. |
| `ENROLLMENT_APPLY` | ORGANIZATION | Học viên tự thực hiện đăng ký môn học trong đợt mở đăng ký. |
| `ENROLLMENT_MANAGE` | ORGANIZATION | Cán bộ giáo vụ xét duyệt, thêm hoặc rút tên học viên khỏi lớp. |

### 4.7. Phân hệ Giảng dạy & Điểm danh (`teaching` & `attendance`)

| Mã quyền | Phạm vi | Mô tả chi tiết |
| :--- | :---: | :--- |
| `TEACHING_DIARY_WRITE` | ORGANIZATION | Giảng viên ghi nhật ký giảng dạy và tài liệu buổi học. |
| `ATTENDANCE_SESSION_MANAGE`| ORGANIZATION | Khởi tạo phiên điểm danh, thiết lập mã QR hoặc mã PIN điểm danh. |
| `ATTENDANCE_RECORD_SUBMIT` | ORGANIZATION | Ghi nhận trạng thái có mặt, vắng mặt hoặc đi muộn của học viên. |
| `ATTENDANCE_REPORT_VIEW`   | ORGANIZATION | Xem báo cáo chuyên cần tổng hợp của lớp học phần. |

### 4.8. Phân hệ Đánh giá & Bảng điểm (`assessment`)

| Mã quyền | Phạm vi | Mô tả chi tiết |
| :--- | :---: | :--- |
| `ASSESSMENT_ITEM_MANAGE`   | ORGANIZATION | Thiết lập cột điểm, tỷ trọng điểm chuyên cần, giữa kỳ và thi cuối kỳ. |
| `ASSESSMENT_GRADE_SUBMIT`  | ORGANIZATION | Giảng viên nhập điểm và chỉnh sửa điểm thành phần trong thời hạn. |
| `ASSESSMENT_GRADE_FINALIZE`| ORGANIZATION | Trưởng bộ môn/giáo vụ phê duyệt bảng điểm và khóa sổ điểm học kỳ. |
| `ASSESSMENT_TRANSCRIPT_READ`| ORGANIZATION| Học sinh/giảng viên xem bảng điểm cá nhân hoặc lớp phụ trách. |

### 4.9. Phân hệ Chứng chỉ & Bằng cấp (`certification`)

| Mã quyền | Phạm vi | Mô tả chi tiết |
| :--- | :---: | :--- |
| `CERT_TEMPLATE_MANAGE`    | ORGANIZATION | Thiết kế mẫu phôi chứng chỉ, chứng nhận tốt nghiệp. |
| `CERT_ISSUE`               | ORGANIZATION | Cấp phát chứng chỉ số có mã xác thực cho học viên hoàn thành khóa học. |
| `CERT_REVOKE`              | ORGANIZATION | Thu hồi chứng chỉ đã cấp do vi phạm quy chế hoặc cấp sai. |

---

## 5. Nguyên tắc Quản lý và Ràng buộc Bất biến

1. **Bất biến (Immutability)**: Mã quyền (`code`) là định danh bất biến một khi đã được đưa vào mã nguồn và database migration. Nghiêm cấm đổi tên mã quyền vì sẽ làm vô hiệu hóa các biểu thức `@PreAuthorize` đang chạy.
2. **Ngăn chặn Custom Permission tự phát**: Tổ chức không được phép tự thêm mã quyền mới ngoài danh mục chuẩn hóa. Tổ chức chỉ được phép tạo Custom Role và lựa chọn các quyền từ danh mục có sẵn.
3. **Validation khi gán quyền**: Khi gán permission cho một Role của tổ chức, hệ thống bắt buộc kiểm tra `permission.scope == 'ORGANIZATION'`. Không cho phép gán quyền cấp `SYSTEM` vào Role của tổ chức.

---

## 6. Tiêu chí Đánh giá & Nghiệm thu (Checklist AC-2.7.2)

- [x] **AC-2.7.2.1**: Cú pháp `DOMAIN_RESOURCE_ACTION` được áp dụng 100% cho mọi mã quyền.
- [x] **AC-2.7.2.2**: Master Permission Catalog bao phủ đầy đủ các thao tác chính của 12 business modules.
- [x] **AC-2.7.2.3**: Phân biệt rành mạch giữa `SYSTEM` scope và `ORGANIZATION` scope.
- [x] **AC-2.7.2.4**: Cơ chế validation ngăn chặn việc gán System Permissions cho Organization Roles.
- [x] **AC-2.7.2.5**: Mã quyền được cố định trong Migration và Enum/Constants của hệ thống.
