package com.edu.sms.modules.grade.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "grade_transcripts",
    uniqueConstraints = @UniqueConstraint(columnNames = {"studentId", "semester"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transcript {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID studentId; // Soft Link -> User.students.id

    @Column(nullable = false)
    private String semester; // e.g. Fall 2026

    @Column(precision = 3, scale = 2)
    private BigDecimal gpa;

    private int totalCredits;

    @CreationTimestamp
    private LocalDateTime issuedAt;
}
