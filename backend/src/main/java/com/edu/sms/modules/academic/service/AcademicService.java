package com.edu.sms.modules.academic.service;

import com.edu.sms.common.exception.ResourceNotFoundException;
import com.edu.sms.modules.academic.dto.AcademicClassRequest;
import com.edu.sms.modules.academic.dto.AcademicClassResponse;
import com.edu.sms.modules.academic.dto.AcademicRequest;
import com.edu.sms.modules.academic.dto.AcademicResponse;
import com.edu.sms.modules.academic.dto.AddStudentsRequest;
import com.edu.sms.modules.academic.dto.ClassStudentResponse;
import com.edu.sms.modules.academic.dto.CourseRequest;
import com.edu.sms.modules.academic.dto.CourseResponse;
import com.edu.sms.modules.academic.dto.ScheduleRequest;
import com.edu.sms.modules.academic.dto.ScheduleResponse;
import com.edu.sms.modules.academic.dto.SemesterRequest;
import com.edu.sms.modules.academic.dto.SemesterResponse;
import com.edu.sms.modules.academic.dto.TeacherAssignmentRequest;
import com.edu.sms.modules.academic.model.AcademicClass;
import com.edu.sms.modules.academic.model.ClassStudent;
import com.edu.sms.modules.academic.model.Course;
import com.edu.sms.modules.academic.model.Schedule;
import com.edu.sms.modules.academic.model.Semester;
import com.edu.sms.modules.academic.model.TeacherAssignment;
import com.edu.sms.modules.academic.repository.AcademicClassRepository;
import com.edu.sms.modules.academic.repository.ClassStudentRepository;
import com.edu.sms.modules.academic.repository.CourseRepository;
import com.edu.sms.modules.academic.repository.ScheduleRepository;
import com.edu.sms.modules.academic.repository.SemesterRepository;
import com.edu.sms.modules.academic.repository.TeacherAssignmentRepository;
import com.edu.sms.modules.user.api.UserInternalApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademicService {

    private final CourseRepository courseRepository;
    private final SemesterRepository semesterRepository;
    private final AcademicClassRepository classRepository;
    private final ClassStudentRepository classStudentRepository;
    private final ScheduleRepository scheduleRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final UserInternalApi userInternalApi;

    @Transactional(readOnly = true)
    public List<AcademicResponse> getAllCourses(UUID semesterId) {
        return (semesterId == null ? courseRepository.findAll() : courseRepository.findOpenCoursesBySemesterId(semesterId)).stream()
                .map(course -> AcademicResponse.builder()
                        .id(course.getId())
                        .name(course.getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourse(UUID courseId) {
        Course course = findCourseById(courseId);
        return mapCourse(course);
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        courseRepository.findByCode(request.getCode())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Mã môn học đã tồn tại");
                });

        Course course = Course.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .credits(request.getCredits())
                .isActive(request.getIsActive() == null ? true : request.getIsActive())
                .build();
        Course saved = courseRepository.save(course);
        return mapCourse(saved);
    }

    @Transactional
    public CourseResponse updateCourse(UUID courseId, CourseRequest request) {
        Course course = findCourseById(courseId);
        if (!course.getCode().equals(request.getCode())) {
            courseRepository.findByCode(request.getCode()).ifPresent(existing -> {
                throw new IllegalArgumentException("Mã môn học đã được sử dụng");
            });
            course.setCode(request.getCode());
        }
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setCredits(request.getCredits());
        course.setIsActive(request.getIsActive() == null ? course.getIsActive() : request.getIsActive());
        return mapCourse(courseRepository.save(course));
    }

    @Transactional
    public void deleteCourse(UUID courseId) {
        Course course = findCourseById(courseId);
        course.setIsActive(false);
        courseRepository.save(course);
    }

    @Transactional(readOnly = true)
    public List<SemesterResponse> getAllSemesters() {
        return semesterRepository.findAll().stream()
                .map(this::mapSemester)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SemesterResponse getSemester(UUID semesterId) {
        return mapSemester(findSemesterById(semesterId));
    }

    @Transactional
    public SemesterResponse createSemester(SemesterRequest request) {
        validateSemesterDates(request.getStartDate(), request.getEndDate());
        Semester semester = Semester.builder()
                .name(request.getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus())
                .build();
        return mapSemester(semesterRepository.save(semester));
    }

    @Transactional
    public SemesterResponse updateSemester(UUID semesterId, SemesterRequest request) {
        Semester semester = findSemesterById(semesterId);
        validateSemesterDates(request.getStartDate(), request.getEndDate());
        semester.setName(request.getName());
        semester.setStartDate(request.getStartDate());
        semester.setEndDate(request.getEndDate());
        semester.setStatus(request.getStatus());
        return mapSemester(semesterRepository.save(semester));
    }

    @Transactional
    public void deleteSemester(UUID semesterId) {
        if (!classRepository.findBySemesterId(semesterId).isEmpty()) {
            throw new IllegalArgumentException("Không thể xoá học kỳ đang có lớp học");
        }
        semesterRepository.deleteById(semesterId);
    }

    @Transactional(readOnly = true)
    public List<AcademicClassResponse> getClasses(UUID semesterId, UUID teacherId) {
        List<AcademicClass> classes;
        if (semesterId != null && teacherId != null) {
            classes = classRepository.findBySemesterId(semesterId).stream()
                    .filter(ac -> teacherId.equals(ac.getTeacherId()))
                    .collect(Collectors.toList());
        } else if (semesterId != null) {
            classes = classRepository.findBySemesterId(semesterId);
        } else if (teacherId != null) {
            classes = classRepository.findByTeacherId(teacherId);
        } else {
            classes = classRepository.findAll();
        }
        return classes.stream().map(this::mapAcademicClass).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AcademicClassResponse getClassById(UUID classId) {
        return mapAcademicClass(findAcademicClassById(classId));
    }

    @Transactional
    public AcademicClassResponse createClass(AcademicClassRequest request) {
        Course course = findCourseById(request.getCourseId());
        Semester semester = findSemesterById(request.getSemesterId());

        if (request.getTeacherId() != null && !userInternalApi.existsTeacher(request.getTeacherId())) {
            throw new IllegalArgumentException("Giáo viên không tồn tại");
        }

        AcademicClass academicClass = AcademicClass.builder()
                .name(request.getName())
                .course(course)
                .semester(semester)
                .teacherId(request.getTeacherId())
                .room(request.getRoom())
                .maxCapacity(request.getMaxCapacity())
                .build();
        return mapAcademicClass(classRepository.save(academicClass));
    }

    @Transactional
    public AcademicClassResponse assignTeacher(UUID classId, TeacherAssignmentRequest request) {
        AcademicClass academicClass = findAcademicClassById(classId);
        if (!userInternalApi.existsTeacher(request.getTeacherId())) {
            throw new IllegalArgumentException("Giáo viên không tồn tại");
        }
        academicClass.setTeacherId(request.getTeacherId());
        classRepository.save(academicClass);

        TeacherAssignment assignment = TeacherAssignment.builder()
                .academicClass(academicClass)
                .teacherId(request.getTeacherId())
                .role("PRIMARY")
                .build();
        teacherAssignmentRepository.save(assignment);

        return mapAcademicClass(academicClass);
    }

    @Transactional
    public List<ClassStudentResponse> addStudents(UUID classId, AddStudentsRequest request) {
        findAcademicClassById(classId);
        Set<UUID> studentIds = request.getStudentIds();
        Map<UUID, String> names = userInternalApi.getStudentNames(studentIds);

        List<ClassStudent> saved = studentIds.stream()
                .peek(studentId -> {
                    if (!userInternalApi.existsStudent(studentId)) {
                        throw new IllegalArgumentException("Học sinh không tồn tại: " + studentId);
                    }
                    if (classStudentRepository.existsByClassIdAndStudentId(classId, studentId)) {
                        throw new IllegalArgumentException("Học sinh đã có trong lớp: " + studentId);
                    }
                })
                .map(studentId -> ClassStudent.builder()
                        .classId(classId)
                        .studentId(studentId)
                        .build())
                .map(classStudentRepository::save)
                .collect(Collectors.toList());

        return saved.stream()
                .map(cs -> ClassStudentResponse.builder()
                        .studentId(cs.getStudentId())
                        .studentName(names.getOrDefault(cs.getStudentId(), "Unknown Student"))
                        .joinedAt(cs.getJoinedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClassStudentResponse> getStudents(UUID classId) {
        findAcademicClassById(classId);
        List<ClassStudent> students = classStudentRepository.findByClassId(classId);
        Set<UUID> studentIds = students.stream().map(ClassStudent::getStudentId).collect(Collectors.toSet());
        Map<UUID, String> names = userInternalApi.getStudentNames(studentIds);
        return students.stream()
                .map(cs -> ClassStudentResponse.builder()
                        .studentId(cs.getStudentId())
                        .studentName(names.getOrDefault(cs.getStudentId(), "Unknown Student"))
                        .joinedAt(cs.getJoinedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleResponse createSchedule(UUID classId, ScheduleRequest request) {
        AcademicClass academicClass = findAcademicClassById(classId);
        validateScheduleTime(request.getStartTime(), request.getEndTime());

        List<Schedule> existing = scheduleRepository.findByAcademicClassId(classId);
        if (existing.stream().anyMatch(schedule -> schedule.getDayOfWeek() == request.getDayOfWeek()
                && intervalsOverlap(schedule.getStartTime(), schedule.getEndTime(), request.getStartTime(), request.getEndTime()))) {
            throw new IllegalArgumentException("Lịch học trùng khoảng thời gian đã tồn tại cho lớp này");
        }

        Schedule schedule = Schedule.builder()
                .academicClass(academicClass)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .room(request.getRoom())
                .build();
        return mapSchedule(scheduleRepository.save(schedule));
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedulesByClass(UUID classId) {
        findAcademicClassById(classId);
        return scheduleRepository.findByAcademicClassId(classId).stream()
                .map(this::mapSchedule)
                .sorted((a, b) -> {
                    int cmp = a.getDayOfWeek().compareTo(b.getDayOfWeek());
                    if (cmp != 0) return cmp;
                    return a.getStartTime().compareTo(b.getStartTime());
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getMySchedule(UUID authUserId) {
        try {
            UUID teacherId = userInternalApi.getTeacherIdByAuthUserId(authUserId);
            List<UUID> classIds = classRepository.findByTeacherId(teacherId).stream()
                    .map(AcademicClass::getId)
                    .collect(Collectors.toList());
            return scheduleRepository.findByAcademicClassIdIn(classIds).stream()
                    .map(this::mapSchedule)
                    .sorted((a, b) -> {
                        int cmp = a.getDayOfWeek().compareTo(b.getDayOfWeek());
                        if (cmp != 0) return cmp;
                        return a.getStartTime().compareTo(b.getStartTime());
                    })
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException ignored) {
            UUID studentId = userInternalApi.getStudentIdByAuthUserId(authUserId);
            List<UUID> classIds = classStudentRepository.findByStudentId(studentId).stream()
                    .map(ClassStudent::getClassId)
                    .collect(Collectors.toList());
            return scheduleRepository.findByAcademicClassIdIn(classIds).stream()
                    .map(this::mapSchedule)
                    .sorted((a, b) -> {
                        int cmp = a.getDayOfWeek().compareTo(b.getDayOfWeek());
                        if (cmp != 0) return cmp;
                        return a.getStartTime().compareTo(b.getStartTime());
                    })
                    .collect(Collectors.toList());
        }
    }

    private Course findCourseById(UUID courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Môn học không tồn tại"));
    }

    private Semester findSemesterById(UUID semesterId) {
        return semesterRepository.findById(semesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Học kỳ không tồn tại"));
    }

    private AcademicClass findAcademicClassById(UUID classId) {
        return classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Lớp học không tồn tại"));
    }

    private CourseResponse mapCourse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .description(course.getDescription())
                .credits(course.getCredits())
                .isActive(course.getIsActive())
                .build();
    }

    private SemesterResponse mapSemester(Semester semester) {
        return SemesterResponse.builder()
                .id(semester.getId())
                .name(semester.getName())
                .startDate(semester.getStartDate())
                .endDate(semester.getEndDate())
                .status(semester.getStatus())
                .build();
    }

    private AcademicClassResponse mapAcademicClass(AcademicClass academicClass) {
        return AcademicClassResponse.builder()
                .id(academicClass.getId())
                .name(academicClass.getName())
                .courseId(academicClass.getCourse().getId())
                .courseName(academicClass.getCourse().getName())
                .semesterId(academicClass.getSemester().getId())
                .semesterName(academicClass.getSemester().getName())
                .teacherId(academicClass.getTeacherId())
                .teacherName(academicClass.getTeacherId() == null ? null : userInternalApi.getTeacherName(academicClass.getTeacherId()))
                .room(academicClass.getRoom())
                .maxCapacity(academicClass.getMaxCapacity())
                .build();
    }

    private ScheduleResponse mapSchedule(Schedule schedule) {
        return ScheduleResponse.builder()
                .id(schedule.getId())
                .classId(schedule.getAcademicClass().getId())
                .className(schedule.getAcademicClass().getName())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .room(schedule.getRoom())
                .build();
    }

    private void validateSemesterDates(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc");
        }
    }

    private void validateScheduleTime(LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Thời gian bắt đầu phải trước thời gian kết thúc");
        }
    }

    private boolean intervalsOverlap(LocalTime existingStart, LocalTime existingEnd, LocalTime newStart, LocalTime newEnd) {
        return !newStart.isAfter(existingEnd) && !existingStart.isAfter(newEnd) && newStart.isBefore(existingEnd) && existingStart.isBefore(newEnd);
    }
}
