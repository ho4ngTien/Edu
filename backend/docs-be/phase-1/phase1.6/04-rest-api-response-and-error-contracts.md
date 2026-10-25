# Phase 1.6 — Thành phần 04: REST API Response & Error Contracts

Tài liệu này đặc tả quy chuẩn hợp đồng dữ liệu cho các REST API của E-Edu, định dạng phản hồi thành công, cấu trúc lỗi chuẩn hóa `ApiErrorResponse` và các quy tắc phân trang.

---

## 1. Cấu Trúc Phản Hồi Thành Công (Successful Response)

### 1.1. Thao tác Tạo mới bản ghi (`POST`)
- Trả về HTTP Status `201 Created`.
- Header `Location`: Chứa URI đến tài nguyên mới tạo (ví dụ: `/api/v1/organizations/{id}`).
- Body chứa đối tượng vừa tạo.

### 1.2. Thao tác Truy vấn danh sách có phân trang (`GET`)
Dữ liệu danh sách luôn được bọc trong cấu trúc phân trang chuẩn:
```json
{
  "content": [
    {
      "id": "e4b52df5-911f-4424-bcae-e722aa046342",
      "name": "Trường THPT Chuyên Lê Hồng Phong",
      "code": "LHP_HCM",
      "status": "ACTIVE",
      "createdAt": "2026-10-25T12:00:00Z"
    }
  ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 1,
  "totalPages": 1,
  "isFirst": true,
  "isLast": true
}
```

---

## 2. Chuẩn Hóa Phản Hồi Lỗi Toàn Cầu: `ApiErrorResponse`

100% các lỗi phát sinh trong hệ thống (từ tầng Web, Validation, Security cho đến Domain) bắt buộc phải trả về cùng một cấu trúc JSON nhất quán:

```json
{
  "timestamp": "2026-10-25T12:30:00Z",
  "status": 400,
  "code": "VALIDATION_FAILED",
  "message": "Dữ liệu yêu cầu không hợp lệ",
  "path": "/api/v1/identity/accounts/register",
  "fieldErrors": [
    {
      "field": "email",
      "message": "must be a well-formed email address"
    },
    {
      "field": "password",
      "message": "size must be between 8 and 100"
    }
  ]
}
```

### Bảng Phân Loại Mã Lỗi Chuẩn:

| HTTP Status | Mã lỗi hệ thống (`code`) | Mô tả chi tiết |
| :---: | :--- | :--- |
| **400** | `VALIDATION_FAILED` | Vi phạm ràng buộc `@Valid` trên DTO đầu vào (kèm mảng `fieldErrors`). |
| **400** | `BAD_REQUEST` | Tham số query không hợp lệ hoặc định dạng JSON sai cú pháp. |
| **401** | `UNAUTHORIZED` | Không có Bearer token, token sai chữ ký, hết hạn hoặc sai mật khẩu. |
| **403** | `FORBIDDEN` | Tài khoản không có vai trò hoặc quyền hạn thực hiện hành vi này. |
| **404** | `RESOURCE_NOT_FOUND` | Không tìm thấy bản ghi với ID được cung cấp. |
| **409** | `DUPLICATE_RESOURCE` | Xung đột dữ liệu duy nhất (trùng email, username, mã tổ chức). |
| **500** | `INTERNAL_ERROR` | Lỗi máy chủ không mong muốn (không làm lộ stack trace ra ngoài client). |
