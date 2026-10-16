-- =============================================================================
-- E-Edu Migration V1: Khởi tạo các Schema cho Kiến trúc Modular Monolith (AD-02)
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS identity;
CREATE SCHEMA IF NOT EXISTS organization;
CREATE SCHEMA IF NOT EXISTS configuration;
CREATE SCHEMA IF NOT EXISTS people;
CREATE SCHEMA IF NOT EXISTS curriculum;
CREATE SCHEMA IF NOT EXISTS academic;
CREATE SCHEMA IF NOT EXISTS enrollment;
CREATE SCHEMA IF NOT EXISTS teaching;
CREATE SCHEMA IF NOT EXISTS attendance;
CREATE SCHEMA IF NOT EXISTS assessment;
CREATE SCHEMA IF NOT EXISTS certification;
CREATE SCHEMA IF NOT EXISTS rule_engine;

COMMENT ON SCHEMA identity IS 'Module Quản lý người dùng, tài khoản, vai trò và phân quyền';
COMMENT ON SCHEMA organization IS 'Module Quản lý cơ cấu trường, phân hiệu, phòng ban và membership';
COMMENT ON SCHEMA configuration IS 'Module Quản lý thiết lập hệ thống và quy tắc nghiệp vụ có kiểu';
COMMENT ON SCHEMA people IS 'Module Quản lý thông tin định danh Person, sinh viên, giảng viên';
COMMENT ON SCHEMA curriculum IS 'Module Quản lý chương trình đào tạo, môn học và khung kiến thức';
COMMENT ON SCHEMA academic IS 'Module Quản lý niên khóa, học kỳ, lớp học phần và thời khóa biểu';
COMMENT ON SCHEMA enrollment IS 'Module Quản lý quy trình đăng ký môn học và nhập học';
COMMENT ON SCHEMA teaching IS 'Module Quản lý phân công giảng dạy và hoạt động sư phạm';
COMMENT ON SCHEMA attendance IS 'Module Quản lý điểm danh và theo dõi chuyên cần';
COMMENT ON SCHEMA assessment IS 'Module Quản lý đầu điểm, kỳ thi và đánh giá kết quả';
COMMENT ON SCHEMA certification IS 'Module Quản lý điều kiện tốt nghiệp và cấp chứng nhận';
COMMENT ON SCHEMA rule_engine IS 'Module Động cơ thẩm định quy tắc nghiệp vụ theo phiên bản (AD-04)';
