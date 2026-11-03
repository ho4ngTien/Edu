# Phase 6 — Academic Operations

Tài liệu kỹ thuật cho phân hệ vận hành học vụ: lịch giảng dạy, điểm danh, kiểm tra-đánh giá, rule engine nghiệp vụ và chứng nhận hoàn thành học phần.

---

## Danh mục Tài liệu Kỹ thuật

| Phase | Tài liệu | Trạng thái | Mô tả |
| :---: | :--- | :---: | :--- |
| **6.1** | [Teaching Schedules & Assignments](phase6.1.md) | 📋 Kế hoạch | Module `teaching`: phân công giảng dạy, thời khóa biểu theo học kỳ |
| **6.2** | [Attendance Recording & Tracking](phase6.2.md) | 📋 Kế hoạch | Module `attendance`: điểm danh từng buổi, tổng kết tỉ lệ chuyên cần |
| **6.3** | [Assessments & Grades](phase6.3.md) | 📋 Kế hoạch | Module `assessment`: bài kiểm tra, nhập điểm, tính điểm tổng kết theo trọng số |
| **6.4** | [Rule Engine Integration](phase6.4.md) | 📋 Kế hoạch | Module `rule-engine`: quy tắc nghiệp vụ động, Composite Rule Engine |
| **6.5** | [Completion & Certification Rules](phase6.5.md) | 📋 Kế hoạch | Module `certification`: đánh giá đủ điều kiện, phát hành CertificationRecord |

---

## Luồng Tích hợp Giữa Các Module

```
[assessment]  ──→ StudentGradeQuery Port ──→
                                              [certification] → RuleEngine → CertificationRecord → StudentCertifiedEvent
[attendance]  ──→ StudentAttendanceQuery Port →
```

Mỗi module giao tiếp qua **Public Port interfaces**, không bao giờ import trực tiếp entity hay repository của nhau.

---

> Xem thêm lộ trình tổng thể tại [Backend Roadmap](../backend-roadmap.md).

