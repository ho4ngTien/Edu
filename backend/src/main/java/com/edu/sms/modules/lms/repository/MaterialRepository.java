package com.edu.sms.modules.lms.repository;

import com.edu.sms.modules.lms.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaterialRepository extends JpaRepository<Material, UUID> {
    List<Material> findByClassId(UUID classId);
}
