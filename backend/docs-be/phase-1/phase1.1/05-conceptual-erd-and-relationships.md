# Phase 1.1 — Thành phần 05: Sơ đồ ERD Mức Khái niệm

Tài liệu này tổng hợp toàn bộ các thực thể cốt lõi trong sơ đồ quan hệ thực thể (ERD) mức khái niệm, thể hiện rõ các ràng buộc then chốt giữa các phân hệ của E-Edu.

---

## 1. Sơ đồ Quan hệ Thực thể Toàn diện (Mermaid ERD)

```mermaid
erDiagram
    %% Identity & Access
    ORGANIZATION ||--o{ ORGANIZATION : parent_subsidiary
    ORGANIZATION ||--o{ ORGANIZATION_MEMBERSHIP : has_members
    USER ||--o{ ORGANIZATION_MEMBERSHIP : joins
    USER ||--o{ USER_ROLE : assigned
    ROLE ||--o{ USER_ROLE : groups
    ROLE }o--o{ PERMISSION : grants

    %% People
    PERSON ||--o| USER : login_account
    PERSON ||--o| STUDENT : student_profile
    PERSON ||--o| TEACHER : teacher_profile
    PERSON ||--o| GUARDIAN : guardian_profile
    STUDENT ||--o{ STUDENT_GUARDIAN : supervised_by
    GUARDIAN ||--o{ STUDENT_GUARDIAN : links_to

    %% Curriculum
    ORGANIZATION ||--o{ PROGRAM : offers
    PROGRAM ||--o{ PROGRAM_VERSION : revisions
    PROGRAM_VERSION ||--o{ PROGRAM_COURSE : contains
    COURSE ||--o{ PROGRAM_COURSE : defined_in

    %% Academic
    ORGANIZATION ||--o{ ACADEMIC_PERIOD : schedules
    ORGANIZATION ||--o{ CLASS_GROUP : groups
    ACADEMIC_PERIOD ||--o{ COURSE_OFFERING : timed_in
    COURSE ||--o{ COURSE_OFFERING : runs_as
    COURSE_OFFERING ||--o{ CLASS_SECTION : partitions
    ROOM ||--o{ CLASS_SECTION : allocated_to
    CLASS_SECTION ||--o{ TEACHING_ASSIGNMENT : staffed_by
    TEACHER ||--o{ TEACHING_ASSIGNMENT : teaches

    %% Enrollment & Learning
    STUDENT ||--o{ PROGRAM_ENROLLMENT : enrolled_into
    PROGRAM_VERSION ||--o{ PROGRAM_ENROLLMENT : receives_students
    STUDENT ||--o{ COURSE_ENROLLMENT : attends
    COURSE_OFFERING ||--o{ COURSE_ENROLLMENT : hosts
    CLASS_SECTION ||--o{ COURSE_ENROLLMENT : places_in

    %% Operations & Assessment
    COURSE_ENROLLMENT ||--o{ ATTENDANCE_RECORD : daily_presence
    COURSE_OFFERING ||--o{ ASSESSMENT : evaluates_by
    ASSESSMENT ||--o{ ASSESSMENT_RESULT : scores
    STUDENT ||--o{ ASSESSMENT_RESULT : gets_score

    %% Certification & Completion
    STUDENT ||--o{ CERTIFICATE : awarded_to
    PROGRAM_VERSION ||--o{ CERTIFICATE : certifies_completion
```

---

## 2. Quy Tắc Ràng Buộc Quan Hệ

### 2.1. Phân tầng Chương trình và Khóa học
1. **`PROGRAM` $\to$ `PROGRAM_VERSION`**: Quan hệ $1 - N$. Một chương trình có thể có nhiều phiên bản. Học sinh nhập học vào niên khóa nào sẽ gắn với phiên bản của niên khóa đó.
2. **`PROGRAM_VERSION` $\leftrightarrow$ `COURSE` thông qua `PROGRAM_COURSE`**: Quan hệ $N - N$ qua thực thể trung gian. `PROGRAM_COURSE` lưu thêm `credits`, `semester_index`, `is_compulsory`, `prerequisite_course_id`.

### 2.2. Phân công và Lịch trình
1. **`COURSE_OFFERING` $\to$ `CLASS_SECTION`**: Quan hệ $1 - N$. Một môn học mở trong học kỳ có thể tách thành nhiều lớp học phần khác nhau về phòng học và giảng viên phụ trách.
2. **`CLASS_SECTION` $\to$ `TEACHING_ASSIGNMENT` $\leftarrow$ `TEACHER`**: Quan hệ $N - N$ qua thực thể phân công. Hỗ trợ trường hợp một lớp học phần có giảng viên chính (Lecturer) và trợ giảng (Teaching Assistant).

### 2.3. Đăng ký và Đánh giá
1. **`PROGRAM_ENROLLMENT`**: Ghi nhận thời điểm học viên trúng tuyển vào một chương trình đào tạo.
2. **`COURSE_ENROLLMENT`**: Ghi nhận việc đăng ký cụ thể vào một lớp học phần của học kỳ. Một học viên chỉ có tối đa một `COURSE_ENROLLMENT` hợp lệ trên mỗi `COURSE_OFFERING`.
3. **`ASSESSMENT` $\to$ `ASSESSMENT_RESULT` $\leftarrow$ `STUDENT`**: Mỗi kết quả đánh giá phải thuộc về một học viên đã đăng ký học phần tương ứng.
