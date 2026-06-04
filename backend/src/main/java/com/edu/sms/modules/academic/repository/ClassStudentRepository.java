package com.edu.sms.modules.academic.repository;

import com.edu.sms.modules.academic.model.ClassStudent;
import com.edu.sms.modules.academic.model.ClassStudentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClassStudentRepository extends JpaRepository<ClassStudent, ClassStudentId> {
    List<ClassStudent> findByClassId(UUID classId);
    List<ClassStudent> findByStudentId(UUID studentId);
    boolean existsByClassIdAndStudentId(UUID classId, UUID studentId);
}
