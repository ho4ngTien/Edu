# Phase 1.7 — Thành phần 03: Security Filter Chain & Request Pipeline

Tài liệu này đặc tả quy trình thực thi của chuỗi bộ lọc bảo mật **Spring Security 6** cho các yêu cầu HTTP trong E-Edu.

---

## 1. Sơ Đồ Xử Lý Của Security Filter Chain

```mermaid
flowchart TD
    REQ["Client HTTP Request<br/>Header: Authorization: Bearer &lt;token&gt;<br/>Header: X-Organization-Id: &lt;uuid&gt;"] --> FILTER_CORS["1. CorsFilter"]
    FILTER_CORS --> FILTER_SEC["2. SecurityContextHolderFilter"]
    FILTER_SEC --> FILTER_BEARER["3. BearerTokenAuthenticationFilter (OAuth2 Resource Server)"]
    
    subgraph JWT_Validation["Xác Thực Chữ Ký & Hạn Token"]
        FILTER_BEARER --> DECODER["NimbusJwtDecoder (HMAC-SHA256)"]
        DECODER --> CLAIMS["Claims Check: sub (userId), iss, aud, exp"]
    end

    CLAIMS --> AUTH_SUCCESS{"Token Hợp lệ?"}
    AUTH_SUCCESS -- Không --> ENTRY_POINT["AuthenticationEntryPoint -> 401 Unauthorized (ApiErrorResponse)"]
    AUTH_SUCCESS -- Có --> SET_SEC["Nạp SecurityContext (Principal: Jwt / UserId)"]
    
    SET_SEC --> FILTER_TENANT["4. TenantResolutionFilter"]
    
    subgraph Tenant_Resolution["Phân Giải Ngữ Cảnh Tổ Chức"]
        FILTER_TENANT --> READ_HEADER["Đọc X-Organization-Id"]
        READ_HEADER --> VERIFY_MEMBERSHIP["Gọi Port: OrganizationMembershipQuery"]
        VERIFY_MEMBERSHIP --> SET_TENANT["Nạp TenantContextHolder"]
    end

    SET_TENANT --> FILTER_AUTHZ["5. AuthorizationFilter (Spring Security)"]
    FILTER_AUTHZ --> PERM_CHECK{"Đủ thẩm quyền?"}
    PERM_CHECK -- Không --> ACCESS_DENIED["AccessDeniedHandler -> 403 Forbidden (ApiErrorResponse)"]
    PERM_CHECK -- Có --> DISPATCHER["DispatcherServlet -> REST Controller"]
```

---

## 2. Cấu Hình Endpoint Mở Công Khai (PermitAll)

Hệ thống tuân thủ nguyên tắc **Mặc định Đóng (Deny-by-default)**: 100% endpoint yêu cầu xác thực Bearer Token, ngoại trừ các endpoint sau được cấu hình `permitAll()`:

1. **Endpoint Xác thực & Tài khoản**:
   - `POST /api/v1/auth/login`: Đăng nhập cấp phát Access Token & Refresh Token.
   - `POST /api/v1/auth/refresh`: Xoay vòng Refresh Token.
   - `POST /api/v1/auth/logout`: Thu hồi token.
   - `POST /api/v1/identity/accounts/register`: Đăng ký tài khoản người dùng mới.
2. **Endpoint Giám sát Hạ tầng**:
   - `GET /actuator/health`: Kiểm tra sức khỏe hệ thống (Liveness & Readiness probe cho Docker/Kubernetes).
   - `GET /actuator/info`: Thông tin phiên bản ứng dụng.
