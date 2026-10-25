# Phase 2.6: Checkpoint 2.6.8 — Security Integration Tests & Verification

> Thuộc [Phase 2.6: Security & JWT Token Management](README.md)

## 11. Checkpoint 2.6.8: Security Integration Tests & Verification

Mục tiêu của checkpoint 2.6.8 là xây dựng bộ kiểm thử tích hợp toàn diện trên môi trường cơ sở dữ liệu PostgreSQL thực tế (Testcontainers), xác minh toàn bộ luồng đăng nhập, refresh token, thu hồi token, kiểm tra tenant isolation, và bảo đảm các quy tắc kiểm soát kiến trúc ArchUnit được tuân thủ 100%.

### 11.1. Ma trận Kịch bản Kiểm thử Bảo mật (Test Matrix)

| Test Suite | File Kiểm thử | Kịch bản Kiểm tra | Kết quả Mong đợi |
| :--- | :--- | :--- | :---: |
| **Authentication Flow** | `AuthenticationIntegrationTest` | Đăng nhập với username đúng & mật khẩu đúng | 200 OK, trả JWT & Refresh Token |
| | | Đăng nhập với email đúng & mật khẩu đúng | 200 OK |
| | | Đăng nhập với mật khẩu không đúng | 401 BAD_CREDENTIALS |
| | | Đăng nhập tài khoản không tồn tại | 401 BAD_CREDENTIALS |
| | | Đăng nhập tài khoản bị khóa (`DISABLED`) | 401 BAD_CREDENTIALS |
| **JWT Verification** | `JwtSecurityIntegrationTest` | Gọi API với Bearer JWT hợp lệ | 200 OK |
| | | Gọi API không kèm Authorization header | 401 UNAUTHORIZED |
| | | Gọi API với Bearer token sai chữ ký | 401 UNAUTHORIZED |
| | | Gọi API với token đã quá hạn (`exp`) | 401 UNAUTHORIZED |
| | | Gọi API với sai `iss` hoặc `aud` | 401 UNAUTHORIZED |
| **Token Rotation** | `RefreshTokenIntegrationTest` | Gửi refresh token hợp lệ | Cấp token mới, token cũ bị thu hồi |
| | | Gửi refresh token đã hết hạn | 401 BAD_CREDENTIALS |
| | | **Token Reuse Detection**: Gửi lại token cũ | Thu hồi toàn bộ token family, trả 401 |
| | | Đăng xuất (`POST /api/v1/auth/logout`) | Token bị thu hồi, refresh thất bại |
| **Tenant & RBAC** | `TenantSecurityIntegrationTest` | Gọi API tổ chức với membership ACTIVE | Thành công |
| | | Truy cập tổ chức mà user không phải thành viên | 403 FORBIDDEN |
| | | Membership ở trạng thái `SUSPENDED` | 403 FORBIDDEN |
| | | Không truyền `X-Organization-Id` trên endpoint tenant | 403 / 400 |
| **Architecture Rules** | `ModularArchitectureTest` | ArchUnit kiểm tra 9 rules ranh giới module | BUILD SUCCESS (0 violation) |

---

### 11.2. Mẫu Integration Test với MockMvc

Vị trí: `backend/tests/integration-tests/src/test/java/com/edu/tests/security/AuthenticationSecurityIntegrationTest.java`

```java
package com.edu.tests.security;

import com.edu.identity.api.dto.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Truy cập endpoint được bảo vệ khi không có Token phải nhận 401")
    void accessingProtectedResourceWithoutTokenReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/organizations")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("Đăng nhập sai mật khẩu phải nhận 401 và không để lộ thông tin cụ thể")
    void loginWithWrongPasswordReturns401() throws Exception {
        LoginRequest request = new LoginRequest("admin@edu.vn", "WrongPassword!");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.code").value("BAD_CREDENTIALS"));
    }
}
```

---

### 11.3. Lệnh Xác minh Toàn diện Hệ thống

Chạy kiểm thử và xác minh toàn bộ 19 modules trong Maven Reactor:

```powershell
mvn clean verify
```

---

