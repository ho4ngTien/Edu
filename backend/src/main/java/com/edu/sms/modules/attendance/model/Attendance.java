package com.edu.sms.modules.attendance.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID classId; // Soft Link -> Academic.classes.id

    @Column(nullable = false)
    private UUID studentId; // Soft Link -> User.students.id

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String status; // PRESENT, ABSENT, LATE, EXCUSED

    private String remarks;

    @Column(nullable = false)
    private UUID recordedBy; // Soft Link -> User.teachers.id

    @CreationTimestamp
    private LocalDateTime createdAt;
}
