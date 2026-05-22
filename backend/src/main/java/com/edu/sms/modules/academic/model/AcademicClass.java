package com.edu.sms.modules.academic.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "academic_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicClass {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(nullable = false)
    private String name; // e.g. CS101-A

    // Soft Link to User Module
    private UUID teacherId;

    private String room;

    private Integer maxCapacity;
}
