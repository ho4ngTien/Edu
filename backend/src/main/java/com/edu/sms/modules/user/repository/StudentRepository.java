package com.edu.sms.modules.user.repository;

import com.edu.sms.modules.user.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {
    Optional<Student> findByStudentCode(String studentCode);

    Optional<Student> findByProfileAuthUserId(UUID authUserId);

    boolean existsByStudentCode(String studentCode);

    @Query("""
            SELECT s FROM Student s JOIN s.profile p
            WHERE (:q IS NULL OR :q = ''
                OR LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Student> search(@Param("q") String q, Pageable pageable);
}
