# Phase 2.5: Phân quyền RBAC & Cô lập Tenant (Multi-Tenancy)

> Thuộc [Phase 2.5: Identity & Organization Core](README.md)

### 4.5. Phân quyền RBAC & Cô lập Tenant (Multi-Tenancy)

#### 1. Ma trận Phân quyền Tổ chức (Action Authorization Matrix)

| Hành động Nghiệp vụ (Action) | `OWNER` | `ADMIN` | `MEMBER` | Điều kiện kiểm tra Tenant Scope |
| :--- | :---: | :---: | :---: | :--- |
| `organization:read` | Yes | Yes | Yes | Người dùng phải có membership `ACTIVE` trong tổ chức |
| `organization:update` | Yes | Yes | No | Yêu cầu vai trò `OWNER` hoặc `ADMIN` |
| `organization:delete` | Yes | No | No | Chỉ `OWNER` được phép giải thể tổ chức |
| `member:list` | Yes | Yes | Yes | Thành viên `ACTIVE` được xem danh bạ tổ chức |
| `member:invite` | Yes | Yes | No | Yêu cầu vai trò `OWNER` hoặc `ADMIN` |
| `member:status:change` | Yes | Yes | No | `ADMIN` không được đình chỉ `OWNER`; `OWNER` toàn quyền |
| `member:role:change` | Yes | No | No | Chỉ `OWNER` được thăng cấp hoặc giáng chức quản trị viên |

#### 2. Quy chuẩn kiểm tra quyền tại Application Layer

```java
// Bad: Tin cậy organization_id gửi từ client mà không đối chiếu membership của user đang đăng nhập
@GetMapping("/organizations/{orgId}/members")
public List<MembershipView> getMembers(@PathVariable UUID orgId) {
    return membershipService.listMembers(orgId); // LỖ HỔNG IDOR: User có thể xem data của mọi trường học
}

// Good: Xác minh user hiện tại thuộc tổ chức và có quyền hợp lệ trước khi truy vấn
@GetMapping("/organizations/{orgId}/members")
public List<MembershipView> getMembers(
        @PathVariable UUID orgId,
        @AuthenticationPrincipal CurrentUser currentUser
) {
    tenantAuthorizationService.verifyAccess(currentUser.userId(), orgId, OrganizationRole.MEMBER);
    return membershipService.listMembers(orgId);
}
```

---
