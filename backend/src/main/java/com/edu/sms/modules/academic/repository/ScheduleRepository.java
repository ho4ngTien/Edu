package com.edu.sms.modules.academic.repository;

import com.edu.sms.modules.academic.model.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
    List<Schedule> findByAcademicClassId(UUID classId);
}
