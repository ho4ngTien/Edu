package com.edu.sms.modules.academic.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "academic_semesters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name; // e.g. Fall 2026

    private LocalDate startDate;

    private LocalDate endDate;

    private String status; // ACTIVE, UPCOMING, COMPLETED
}
