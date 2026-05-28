# Backend Checklist: Module 04 - LMS (Learning Management)

**Trạng thái:** ⏳ Pending
**Tiến độ:** 0%

## Domain Entities & Repositories
- [ ] Khởi tạo `Lesson`, `Material`, `Assignment`, `Submission`.
- [ ] Thiết lập Repositories cho các Entity.

## API: Bài giảng & Tài liệu
- [ ] `POST /api/lms/classes/{classId}/lessons`: Giáo viên tạo cấu trúc bài giảng (Chương, Bài).
- [ ] `POST /api/lms/lessons/{lessonId}/materials`: Upload tài liệu (Tích hợp Storage Service lưu file).
- [ ] `GET /api/lms/classes/{classId}/lessons`: Lấy toàn bộ cây bài giảng của một lớp.

## API: Bài tập & Nộp bài
- [ ] `POST /api/lms/lessons/{lessonId}/assignments`: Giáo viên giao bài tập (Lưu title, description, deadline).
- [ ] `POST /api/lms/assignments/{assignmentId}/submit`: Học sinh nộp bài (Nhận Multipart file hoặc text).
- [ ] `GET /api/lms/assignments/{assignmentId}/submissions`: Giáo viên lấy danh sách đã nộp/chưa nộp.
