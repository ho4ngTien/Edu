package com.edu.sms.modules.academic.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "academic_courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code; // e.g. CS101

    @Column(nullable = false)
    private String name;

    private String description;

    private Integer credits;
}
