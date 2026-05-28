package com.edu.sms.modules.grade.repository;

import com.edu.sms.modules.grade.model.Transcript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TranscriptRepository extends JpaRepository<Transcript, UUID> {
    Optional<Transcript> findByStudentIdAndSemester(UUID studentId, String semester);
}
