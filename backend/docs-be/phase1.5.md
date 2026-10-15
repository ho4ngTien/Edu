Phase 1.5 — Persistence & Database Design của E-Edu.
Mục tiêu là thiết kế mô hình dữ liệu PostgreSQL phù hợp với các quyết định đã chốt: Java 21, Spring Boot, Modular Monolith, schema riêng theo module, shared tables theo organization_id và rule engine tổng quát.
Ở phase này, mình sẽ đi theo thứ tự:
1. Chốt quy ước persistence và định danh dữ liệu.
2. Xác định entity, quan hệ và quyền sở hữu bảng theo module.
3. Thiết kế tenant isolation và các ràng buộc toàn vẹn.
4. Xác định chiến lược Flyway migration.
5. Kiểm tra mô hình dữ liệu trước khi tạo DDL.
Phase 1.5 — Persistence & Database Design
1. Chốt quy ước dữ liệu
Mình đề xuất áp dụng các quy ước sau cho toàn bộ E-Edu.
Hạng mục	Quy ước
Database	Một PostgreSQL database
Schema	Mỗi module sở hữu một schema
Primary key	UUID
Tên bảng	snake_case, số nhiều
Tên cột	snake_case
Thời gian	timestamptz, lưu thời điểm theo UTC
Migration	Flyway, migration chỉ tiến về phía trước
Tenant isolation	organization_id trên các bảng dữ liệu thuộc tổ chức
Xóa dữ liệu	Ưu tiên quy trình xóa rõ ràng; soft delete khi nghiệp vụ cần
Optimistic locking	Dùng version cho những entity có nguy cơ cập nhật đồng thời
Audit	created_at, updated_at; bổ sung người tạo/cập nhật khi cần
Một số lưu ý quan trọng:
- Không phải bảng nào cũng cần organization_id. Ví dụ, bảng mô tả một loại toán tử dùng chung của rule engine có thể là dữ liệu hệ thống.
- Không dùng organization_id làm primary key của mọi bảng. Một bản ghi thường có id riêng và thêm organization_id để phân vùng nghiệp vụ.
- Không dùng Double cho điểm số hoặc giá trị cần độ chính xác thập phân; cân nhắc BigDecimal ở Java và numeric ở PostgreSQL.
- Không lưu ngày học, ngày sinh và thời điểm sự kiện bằng cùng một kiểu dữ liệu. Ngày không có giờ nên dùng date; thời điểm thực tế nên dùng timestamptz.
2. Phân chia schema và quyền sở hữu dữ liệu

E-Edu PostgreSQL
Một database · nhiều schema · module tự sở hữu dữ liệu



identity
users, roles, permissions


organization
organizations, memberships


people
persons, students, teachers


curriculum
programs, courses


academic
periods, offerings, classes


assessment
assessments, results


rule_engine
rule definitions, versions


Các schema khác
enrollment, attendance, teaching, certification, configuration




Nguyên tắc quan trọng: một schema không đồng nghĩa với một microservice. E-Edu vẫn là một ứng dụng Modular Monolith, cùng triển khai và có thể dùng chung transaction khi cần. Ranh giới module là ranh giới sở hữu dữ liệu và logic, không phải ranh giới mạng.
3. Mô hình entity và quan hệ
Đây là bản mô hình khởi đầu. Ta phân biệt rõ danh tính con người, tài khoản đăng nhập, hồ sơ học tập, chương trình đào tạo và lần triển khai khóa học.

E-Edu PostgreSQL
Một database · nhiều schema · module tự sở hữu dữ liệu



identity
users, roles, permissions


organization
organizations, memberships


people
persons, students, teachers


curriculum
programs, courses


academic
periods, offerings, classes


assessment
assessments, results


rule_engine
rule definitions, versions


Các schema khác
enrollment, attendance, teaching, certification, configuration

erDiagram
    ORGANIZATIONS ||--o{ ORGANIZATION_MEMBERSHIPS : has
    USERS ||--o{ ORGANIZATION_MEMBERSHIPS : joins
    USERS ||--o{ USER_ROLES : assigned
    ROLES ||--o{ USER_ROLES : contains

    PERSONS ||--o{ STUDENTS : has_profile
    PERSONS ||--o{ TEACHERS : has_profile
    PERSONS ||--o{ GUARDIANS : has_profile

    ORGANIZATIONS ||--o{ STUDENTS : enrolls
    ORGANIZATIONS ||--o{ TEACHERS : employs
    ORGANIZATIONS ||--o{ PROGRAMS : offers
    ORGANIZATIONS ||--o{ ACADEMIC_PERIODS : schedules

    PROGRAMS ||--o{ PROGRAM_VERSIONS : versions
    PROGRAM_VERSIONS ||--o{ PROGRAM_COURSES : contains
    COURSES ||--o{ PROGRAM_COURSES : included_in

    PROGRAM_VERSIONS ||--o{ PROGRAM_ENROLLMENTS : enrollments
    STUDENTS ||--o{ PROGRAM_ENROLLMENTS : joins

    ACADEMIC_PERIODS ||--o{ COURSE_OFFERINGS : includes
    COURSES ||--o{ COURSE_OFFERINGS : delivered_as
    COURSE_OFFERINGS ||--o{ CLASS_SECTIONS : divided_into

    COURSE_OFFERINGS ||--o{ COURSE_ENROLLMENTS : registers
    STUDENTS ||--o{ COURSE_ENROLLMENTS : takes

    CLASS_SECTIONS ||--o{ TEACHING_ASSIGNMENTS : staffed_by
    TEACHERS ||--o{ TEACHING_ASSIGNMENTS : teaches

    COURSE_ENROLLMENTS ||--o{ ATTENDANCE_RECORDS : attendance
    COURSE_ENROLLMENTS ||--o{ ASSESSMENT_RESULTS : results
    ASSESSMENTS ||--o{ ASSESSMENT_RESULTS : records

    ASSESSMENT_SCHEMES ||--o{ ASSESSMENTS : defines
    PROGRAM_ENROLLMENTS ||--o{ COMPLETION_RECORDS : completion
    COMPLETION_RECORDS ||--o| CERTIFICATES : may_issue

Các quyết định domain cần giữ
- Person khác User: một người không nhất thiết phải có tài khoản đăng nhập.
- Student và Teacher là hồ sơ theo ngữ cảnh tổ chức; một người có thể mang nhiều vai trò.
- Program khác Course: chương trình chứa các học phần hoặc khóa học theo một phiên bản cụ thể.
- Course khác CourseOffering: một khóa học có thể được mở nhiều lần ở các kỳ học khác nhau.
- ClassSection là nhóm học cụ thể trong một lần mở khóa học; không nên nhầm với lớp chủ nhiệm ClassGroup.
- ProgramEnrollment khác CourseEnrollment: một người có thể đăng ký chương trình rồi đăng ký từng khóa học.
- AssessmentResult phải gắn với bài đánh giá và người học hoặc lượt đăng ký học cụ thể; không nên lưu một trường grade duy nhất trên bảng students.
Sơ đồ này mô tả các quan hệ nghiệp vụ chính, chưa phải DDL hoàn chỉnh. Các bảng trung gian, khóa ngoại và phạm vi tổ chức sẽ được chốt trong bước tiếp theo.
4. Thiết kế tenant isolation
Vì E-Edu dùng chung bảng giữa các tổ chức, đây là một yêu cầu bảo mật nền tảng, không phải một tính năng có thể bổ sung sau.
Ví dụ, people.students có thể bắt đầu như sau:

CREATE TABLE people.students (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    person_id UUID NOT NULL,
    student_number VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_students_org_id
        UNIQUE (organization_id, id),

    CONSTRAINT uq_students_org_number
        UNIQUE (organization_id, student_number)
);

CREATE INDEX idx_students_org_status
    ON people.students (organization_id, status);



Đây là ví dụ về cách đặt tên và thiết kế khóa, chưa phải migration chạy ngay vì bảng people.persons và các schema liên quan chưa được tạo.
Các nguyên tắc cần thống nhất:
1. Mã học sinh chỉ cần duy nhất trong tổ chức nếu nghiệp vụ không yêu cầu duy nhất toàn hệ thống.
2. Mọi truy vấn hồ sơ học sinh đều phải giới hạn tổ chức theo quyền của người đang thao tác.
3. Khi cần bảo đảm hai bản ghi thuộc cùng tổ chức, dùng composite foreign key hoặc một cơ chế kiểm tra toàn vẹn tương đương.
4. Các API không được cho phép đổi organization_id tùy ý.
5. Nếu dùng PostgreSQL Row-Level Security (RLS), cần thiết kế cách truyền tenant context đáng tin cậy qua connection pool và kiểm thử cả các đường truy cập đặc quyền.
Một lưu ý: UNIQUE (organization_id, id) hữu ích làm đích tham chiếu cho composite foreign key, dù id đã là primary key. Đổi lại, nếu áp dụng rộng rãi, các khóa ngoại và migration sẽ phức tạp hơn. Chỉ nên dùng composite FK ở những quan hệ cần cưỡng chế cùng tổ chức ở tầng database.
5. Persistence của Rule Engine
Với AD-04, rule_engine nên có mô hình dữ liệu riêng, tối thiểu gồm:
Bảng	Mục đích
rule_engine.rule_definitions	Danh tính quy tắc, loại nghiệp vụ, phạm vi áp dụng
rule_engine.rule_versions	Nội dung quy tắc theo từng phiên bản
rule_engine.rule_bindings	Gắn quy tắc với một nghiệp vụ hoặc cấu hình cụ thể
rule_engine.rule_test_cases	Các bộ input/output dùng để kiểm thử quy tắc
rule_engine.rule_evaluations	Lịch sử đánh giá khi nghiệp vụ cần lưu vết
Một quy tắc cần có tối thiểu id, organization_id nếu là quy tắc riêng của tổ chức, version, status, effective_from, effective_to khi có hiệu lực theo thời gian, cùng thông tin kiểm toán.
Nên phân biệt quy tắc dùng chung của nền tảng với quy tắc do tổ chức tùy chỉnh. Không nên dùng organization_id = NULL một cách tùy tiện để biểu diễn mọi trường hợp toàn cục; có thể thiết kế scope_type rõ ràng, chẳng hạn PLATFORM và ORGANIZATION, kèm constraint phù hợp.
Rule engine không nên lưu và thực thi mã Java tùy ý. Hãy dùng một biểu thức có kiểu, giới hạn toán tử, hàm cho phép, thời gian thực thi và độ phức tạp. Kết quả đánh giá quan trọng cần lưu phiên bản quy tắc đã sử dụng để có thể giải thích lại quyết định trong tương lai.
6. Chiến lược Flyway
Mỗi module sở hữu migration của schema mình. Ví dụ:
e-edu-modules/
├── identity/src/main/resources/db/migration/
├── organization/src/main/resources/db/migration/
├── people/src/main/resources/db/migration/
├── academic/src/main/resources/db/migration/
├── assessment/src/main/resources/db/migration/
└── rule-engine/src/main/resources/db/migration/


Trước khi triển khai, cần quyết định cách ứng dụng chính phát hiện và chạy tất cả migration. Có thể dùng một quy trình migration trung tâm với các location được cấu hình rõ ràng, hoặc một cơ chế điều phối migration được kiểm thử riêng.
Không nên để nhiều Flyway instance cùng cập nhật chung một bảng lịch sử migration một cách không chủ ý. Cần xác định rõ nơi lưu lịch sử, cách phân biệt migration của từng module và thứ tự chạy khi có phụ thuộc giữa các schema.
## 7. Checklist nghiệm thu Phase 1.5

**Tiến độ Nghiệm thu Phase 1.5 (6/6)**

- [x] Thống nhất UUID, kiểu thời gian, naming convention và audit fields
- [x] Xác định entity cùng quyền sở hữu bảng theo từng module
- [x] Rà soát các quan hệ và composite foreign key liên quan tenant
- [x] Chốt schema riêng và chiến lược chạy Flyway
- [x] Thiết kế rule definitions, versions và test cases
- [x] Kiểm thử tenant isolation và toàn vẹn dữ liệu

---
Rà soát và hoàn thiện database design
Bước tiếp theo sau Phase 1.5: Phase 1.6 — API Contracts & Cross-Module Communication. Ta sẽ xác định các facade, DTO và luồng nghiệp vụ giữa identity, people, enrollment, assessment, rule-engine cùng các module còn lại, rồi mới chuyển sang dựng skeleton Spring Boot và migration thực tế.