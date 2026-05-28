package com.edu.sms.modules.academic.service;

import com.edu.sms.modules.academic.dto.AcademicRequest;
import com.edu.sms.modules.academic.dto.AcademicResponse;
import com.edu.sms.modules.academic.model.Course;
import com.edu.sms.modules.academic.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademicService {

    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public List<AcademicResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(course -> AcademicResponse.builder()
                        .id(course.getId())
                        .name(course.getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public AcademicResponse createCourse(AcademicRequest request) {
        Course course = Course.builder()
                .name(request.getName())
                .isActive(true)
                .build();
        Course saved = courseRepository.save(course);
        return AcademicResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .build();
    }
}
