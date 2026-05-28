package com.edu.sms.modules.academic.repository;

import com.edu.sms.modules.academic.model.AcademicClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AcademicClassRepository extends JpaRepository<AcademicClass, UUID> {
}
