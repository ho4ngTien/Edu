package com.edu.sms.modules.user.repository;

import com.edu.sms.modules.user.model.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, UUID> {
    Optional<Teacher> findByTeacherCode(String teacherCode);

    boolean existsByTeacherCode(String teacherCode);

    @Query("""
            SELECT t FROM Teacher t JOIN t.profile p
            WHERE (:q IS NULL OR :q = ''
                OR LOWER(t.teacherCode) LIKE LOWER(CONCAT('%', :q, '%'))
                OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Teacher> search(@Param("q") String q, Pageable pageable);
}
