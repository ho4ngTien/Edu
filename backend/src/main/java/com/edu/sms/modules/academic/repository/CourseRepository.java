package com.edu.sms.modules.academic.repository;

import com.edu.sms.modules.academic.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
    Optional<Course> findByCode(String code);

    @Query("""
            SELECT DISTINCT c FROM AcademicClass ac JOIN ac.course c
            WHERE ac.semester.id = :semesterId AND c.isActive = true
            """)
    List<Course> findOpenCoursesBySemesterId(@Param("semesterId") UUID semesterId);
}
