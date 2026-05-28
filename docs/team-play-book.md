# Team Play Book — EduSMS

> Nơi ghi lại **kỹ năng thực chiến** và **bài học kinh nghiệm** trong quá trình làm việc.
> Mỗi mục là tình huống thực tế đã gặp — đọc để không lặp lại sai lầm.

---

## 1. Git Workflow

### 1.1 Commit từng file riêng lẻ — tránh bẫy Staging Area

**Vấn đề:** Nếu đã có file đang ở trạng thái **staged** (cột đầu trong `git status` hiện `A` hoặc `M`), chạy `git commit` sẽ commit **tất cả** file đó — kể cả file bạn không có ý định commit lần này.

**Quy trình đúng:**

```bash
# Bước 1: Xem toàn bộ trạng thái
git status

# Bước 2: Unstage TẤT CẢ — làm sạch staging area
git reset HEAD

# Bước 3: Stage DUY NHẤT file muốn commit
git add <đường-dẫn/tên-file>

# Bước 4: Commit ngay, lặp lại Bước 3–4 cho file tiếp theo
git commit -m "<message>"
```

> ⚠️ **PowerShell:** Dùng `;` thay cho `&&` vì PowerShell không hỗ trợ `&&` làm statement separator.

### 1.2 Hiểu trạng thái 2 cột trong `git status`

Output `git status --short` có dạng `XY filename`:
- Cột `X` (trái) = trạng thái trong **Staging Area** (đã `git add`)
- Cột `Y` (phải) = trạng thái trong **Working Tree** (chưa `git add`)

| Status | Ý nghĩa |
|--------|---------|
| `A ` | File mới, đã staged |
| `AM` | File mới, đã staged, nhưng có thêm thay đổi mới chưa staged |
| `M ` | File cũ đã sửa, đã staged |
| `MM` | File cũ đã sửa, đã staged, và lại có thêm thay đổi mới chưa staged |
| ` M` | File cũ đã sửa, **chưa staged** |

> Chỉ file có ký tự ở **cột trái** mới được đưa vào commit.

### 1.3 Commit Message Convention

```
<type>(<scope>): <mô tả ngắn>
```

| Type | Dùng khi |
|------|---------|
| `feat` | Thêm tính năng mới |
| `fix` | Sửa bug |
| `config` | Thay đổi cấu hình (application.properties, yml…) |
| `build` | Thay đổi build tool (pom.xml, package.json…) |
| `infra` | Thay đổi hạ tầng (docker, nginx…) |
| `refactor` | Tái cấu trúc, không thêm tính năng / không sửa bug |
| `docs` | Cập nhật tài liệu |
| `chore` | Việc lặt vặt (gitignore, format…) |

### 1.4 Quy tắc đặt tên Branch

| Prefix | Dùng khi |
|--------|---------|
| `feature/` | Tính năng mới |
| `bugfix/` | Sửa bug |
| `refactor/` | Tái cấu trúc |
| `hotfix/` | Sửa lỗi khẩn |

### 1.5 Merge vào develop

```bash
# Pull develop mới nhất về branch hiện tại trước khi merge
git fetch origin
git rebase origin/develop

# Giải quyết conflict nếu có
git rebase --continue

# Push và tạo Pull Request trên GitHub
git push origin feature/<tên-tính-năng>
```

> Không merge trực tiếp vào `develop` mà không có review.

### 1.6 Khi gặp Conflict

1. Đọc kỹ từng đoạn conflict (`<<<<<<<`, `=======`, `>>>>>>>`)
2. Trao đổi với người viết đoạn code kia nếu không chắc
3. Sau khi resolve: `git add <file>` → `git rebase --continue`
4. **Không xóa code của người khác mà không hỏi**

### 1.7 Khi nào dùng `git push --force`

> ⚠️ Không dùng `--force` lên `develop` hoặc `main` khi có người khác đang làm cùng branch.

Nếu cần rewrite history đã push trên feature branch cá nhân:
1. Thực hiện `git rebase -i` hoặc `git commit --amend`
2. Dùng `git push --force-with-lease` (an toàn hơn `--force`)
3. Nếu branch dùng chung → thông báo team pull lại: `git pull --rebase`

---

## 2. Checklist Trước Khi Commit

Tự review trước khi `git add`:

- [ ] Code chạy được, không có lỗi compile
- [ ] Không để lại `System.out.println` / `console.log` debug
- [ ] Không commit file `.env`, secret key, password
- [ ] Không commit file build (`target/`, `dist/`, `node_modules/`)
- [ ] Tên biến, method rõ ràng — không dùng `temp`, `abc`, `test123`
- [ ] Xử lý null / exception đầy đủ
- [ ] API endpoint trả về đúng HTTP status code

---

## 3. Debug Tips

### 3.1 Backend (Java Spring Boot)

| Lỗi | Nguyên nhân | Cách fix |
|-----|------------|---------|
| `401 Unauthorized` | JWT token sai / hết hạn | Lấy token mới, kiểm tra `Authorization: Bearer <token>` |
| `403 Forbidden` | Thiếu role/permission | Kiểm tra `SecurityConfig` và role của user |
| `Table not found` | DB chưa có schema | Kiểm tra `spring.jpa.hibernate.ddl-auto` |

Bật log chi tiết trong `application.properties` (nhớ tắt trước khi merge):
```properties
logging.level.org.springframework.security=DEBUG
logging.level.com.edu.sms=DEBUG
```

Khi test API, luôn kiểm tra đủ 4 case:
- Happy path (đúng input)
- Invalid input (thiếu field, sai format)
- Unauthorized (không có token)
- Wrong role (có token nhưng không đủ quyền)

### 3.2 Frontend (React + Vite)

| Lỗi | Nguyên nhân | Cách fix |
|-----|------------|---------|
| `CORS error` | Backend chưa config CORS | Kiểm tra `@CrossOrigin` hoặc `SecurityConfig` |
| `401` khi gọi API | Token hết hạn / không gửi | Kiểm tra interceptor trong `api.ts` |
| Component không re-render | State bị mutate trực tiếp | Tạo object/array mới thay vì mutate |
| `Cannot read properties of undefined` | Data chưa load đã render | Thêm optional chaining `?.` hoặc kiểm tra loading state |

Dùng **DevTools → Network tab** để xem request headers, body và response trước khi đoán nguyên nhân lỗi.

---

## 4. Làm việc Nhóm

### 4.1 Khi bị block

1. Tự debug **30 phút** trước khi hỏi
2. Khi hỏi: nêu rõ **đã làm gì**, **lỗi gì**, **đã thử cách nào**
3. Đừng chỉ hỏi "bị lỗi, giúp với" — không ai biết context của bạn

### 4.2 Code Review

- Review trong **24h** (ngày làm việc)
- Comment rõ dòng nào, sai ở đâu, đề xuất sửa thế nào
- Phân biệt `[blocking]` (phải sửa trước merge) và `[nit]` (nhỏ, tùy bạn)

---

*Cập nhật lần cuối: 2026-05-28*
