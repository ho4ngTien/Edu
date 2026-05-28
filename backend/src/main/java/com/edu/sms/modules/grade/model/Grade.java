package com.edu.sms.modules.grade.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "grade_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID submissionId; // Soft Link -> LMS.submissions.id (Optional)

    @Column(nullable = false)
    private UUID assignmentId; // Soft Link -> LMS.assignments.id

    @Column(nullable = false)
    private UUID studentId; // Soft Link -> User.students.id

    @Column(nullable = false, precision = 4, scale = 2)
    private BigDecimal score;

    private String feedback;

    @Column(nullable = false)
    private UUID gradedBy; // Soft Link -> User.teachers.id

    @CreationTimestamp
    private LocalDateTime gradedAt;
}
