# Phase 2.4 — Thành phần 02: AuditableEntity & Persistence Foundation

Tài liệu này đặc tả lớp cha `@MappedSuperclass` `AuditableEntity` và cấu hình Spring Data JPA Auditing.

---

## 1. Lớp Cha `AuditableEntity.java`

Tệp đặt tại: `shared/shared-kernel/src/main/java/com/edu/shared/kernel/persistence/AuditableEntity.java`

```java
package com.edu.shared.kernel.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
```

---

## 2. Kích Hoạt JPA Auditing & Cấu Hình `Clock`

### 2.1. Cấu hình JPA Auditing (`boot/src/main/java/com/edu/boot/config/JpaAuditingConfig.java`):
```java
package com.edu.boot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider")
public class JpaAuditingConfig {
}
```

### 2.2. Cấu hình DateTimeProvider & UTC Clock (`boot/src/main/java/com/edu/boot/config/DateTimeConfig.java`):
```java
package com.edu.boot.config;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;

@Configuration
public class DateTimeConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public DateTimeProvider dateTimeProvider(Clock clock) {
        return () -> Optional.of(Instant.now(clock));
    }
}
```
- Việc inject `Clock` vào Spring Context cho phép các bài kiểm thử tự động mock thời gian cố định, bảo đảm tính tất định (deterministic) khi test các nghiệp vụ liên quan đến hạn nộp bài thi hoặc hết hạn token.
