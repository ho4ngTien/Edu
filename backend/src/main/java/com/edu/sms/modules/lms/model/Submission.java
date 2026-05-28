package com.edu.sms.modules.lms.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "lms_submissions",
    uniqueConstraints = @UniqueConstraint(columnNames = {"assignmentId", "studentId"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Column(nullable = false)
    private UUID studentId; // Soft Link -> User.students.id

    @Column(columnDefinition = "TEXT")
    private String content;

    private String fileUrl;

    @CreationTimestamp
    private LocalDateTime submittedAt;
}
