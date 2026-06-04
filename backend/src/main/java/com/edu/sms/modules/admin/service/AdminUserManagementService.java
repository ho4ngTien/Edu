package com.edu.sms.modules.admin.service;

import com.edu.sms.common.dto.PageResponse;
import com.edu.sms.common.exception.ResourceNotFoundException;
import com.edu.sms.modules.admin.dto.*;
import com.edu.sms.modules.auth.model.AuthUser;
import com.edu.sms.modules.auth.model.Role;
import com.edu.sms.modules.auth.repository.AuthUserRepository;
import com.edu.sms.modules.auth.repository.RoleRepository;
import com.edu.sms.modules.user.model.Profile;
import com.edu.sms.modules.user.model.Student;
import com.edu.sms.modules.user.model.Teacher;
import com.edu.sms.modules.user.repository.ProfileRepository;
import com.edu.sms.modules.user.repository.StudentRepository;
import com.edu.sms.modules.user.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserManagementService {

    private final AuthUserRepository authUserRepository;
    private final RoleRepository roleRepository;
    private final ProfileRepository profileRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── Students ─────────────────────────────────────────────────────────

    @Transactional
    public StudentAdminResponse createStudent(StudentAdminRequest request) {
        validateUniqueAuthFields(request.getEmail(), request.getUsername(), request.getCccd());
        if (studentRepository.existsByStudentCode(request.getStudentCode())) {
            throw new IllegalArgumentException("Mã học sinh đã tồn tại");
        }

        Role studentRole = roleRepository.findByName("ROLE_STUDENT")
                .orElseThrow(() -> new IllegalStateException("ROLE_STUDENT chưa được khởi tạo"));

        AuthUser authUser = authUserRepository.save(AuthUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .cccd(request.getCccd())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(studentRole))
                .isActive(true)
                .build());

        Profile profile = profileRepository.save(Profile.builder()
                .authUserId(authUser.getId())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .build());

        Student student = studentRepository.save(Student.builder()
                .profile(profile)
                .studentCode(request.getStudentCode())
                .enrollmentDate(request.getEnrollmentDate())
                .build());

        return mapStudent(student, authUser);
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentAdminResponse> listStudents(String search, Pageable pageable) {
        String q = StringUtils.hasText(search) ? search.trim() : null;
        Page<Student> page = studentRepository.search(q, pageable);
        return PageResponse.from(page.map(s -> mapStudent(s, loadAuthUser(s.getProfile().getAuthUserId()))));
    }

    @Transactional(readOnly = true)
    public StudentAdminResponse getStudent(UUID id) {
        Student student = findStudent(id);
        return mapStudent(student, loadAuthUser(student.getProfile().getAuthUserId()));
    }

    @Transactional
    public StudentAdminResponse updateStudent(UUID id, StudentAdminUpdateRequest request) {
        Student student = findStudent(id);
        Profile profile = student.getProfile();
        AuthUser authUser = loadAuthUser(profile.getAuthUserId());

        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(authUser.getEmail())) {
            if (authUserRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email đã được sử dụng");
            }
            authUser.setEmail(request.getEmail());
        }
        if (request.getActive() != null) {
            authUser.setActive(request.getActive());
        }
        authUserRepository.save(authUser);

        if (StringUtils.hasText(request.getFullName())) {
            profile.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }
        profileRepository.save(profile);

        if (StringUtils.hasText(request.getStudentCode())
                && !request.getStudentCode().equals(student.getStudentCode())) {
            if (studentRepository.existsByStudentCode(request.getStudentCode())) {
                throw new IllegalArgumentException("Mã học sinh đã tồn tại");
            }
            student.setStudentCode(request.getStudentCode());
        }
        if (request.getEnrollmentDate() != null) {
            student.setEnrollmentDate(request.getEnrollmentDate());
        }
        studentRepository.save(student);

        return mapStudent(student, authUser);
    }

    @Transactional
    public void deleteStudent(UUID id) {
        Student student = findStudent(id);
        AuthUser authUser = loadAuthUser(student.getProfile().getAuthUserId());
        authUser.setActive(false);
        authUserRepository.save(authUser);
    }

    // ─── Teachers ─────────────────────────────────────────────────────────

    @Transactional
    public TeacherAdminResponse createTeacher(TeacherAdminRequest request) {
        validateUniqueAuthFields(request.getEmail(), request.getUsername(), request.getCccd());
        if (teacherRepository.existsByTeacherCode(request.getTeacherCode())) {
            throw new IllegalArgumentException("Mã giáo viên đã tồn tại");
        }

        Role teacherRole = roleRepository.findByName("ROLE_TEACHER")
                .orElseThrow(() -> new IllegalStateException("ROLE_TEACHER chưa được khởi tạo"));

        AuthUser authUser = authUserRepository.save(AuthUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .cccd(request.getCccd())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(teacherRole))
                .isActive(true)
                .build());

        Profile profile = profileRepository.save(Profile.builder()
                .authUserId(authUser.getId())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .build());

        Teacher teacher = teacherRepository.save(Teacher.builder()
                .profile(profile)
                .teacherCode(request.getTeacherCode())
                .department(request.getDepartment())
                .hireDate(request.getHireDate())
                .build());

        return mapTeacher(teacher, authUser);
    }

    @Transactional(readOnly = true)
    public PageResponse<TeacherAdminResponse> listTeachers(String search, Pageable pageable) {
        String q = StringUtils.hasText(search) ? search.trim() : null;
        Page<Teacher> page = teacherRepository.search(q, pageable);
        return PageResponse.from(page.map(t -> mapTeacher(t, loadAuthUser(t.getProfile().getAuthUserId()))));
    }

    @Transactional(readOnly = true)
    public TeacherAdminResponse getTeacher(UUID id) {
        Teacher teacher = findTeacher(id);
        return mapTeacher(teacher, loadAuthUser(teacher.getProfile().getAuthUserId()));
    }

    @Transactional
    public TeacherAdminResponse updateTeacher(UUID id, TeacherAdminUpdateRequest request) {
        Teacher teacher = findTeacher(id);
        Profile profile = teacher.getProfile();
        AuthUser authUser = loadAuthUser(profile.getAuthUserId());

        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(authUser.getEmail())) {
            if (authUserRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email đã được sử dụng");
            }
            authUser.setEmail(request.getEmail());
        }
        if (request.getActive() != null) {
            authUser.setActive(request.getActive());
        }
        authUserRepository.save(authUser);

        if (StringUtils.hasText(request.getFullName())) {
            profile.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }
        profileRepository.save(profile);

        if (StringUtils.hasText(request.getTeacherCode())
                && !request.getTeacherCode().equals(teacher.getTeacherCode())) {
            if (teacherRepository.existsByTeacherCode(request.getTeacherCode())) {
                throw new IllegalArgumentException("Mã giáo viên đã tồn tại");
            }
            teacher.setTeacherCode(request.getTeacherCode());
        }
        if (request.getDepartment() != null) {
            teacher.setDepartment(request.getDepartment());
        }
        if (request.getHireDate() != null) {
            teacher.setHireDate(request.getHireDate());
        }
        teacherRepository.save(teacher);

        return mapTeacher(teacher, authUser);
    }

    @Transactional
    public void deleteTeacher(UUID id) {
        Teacher teacher = findTeacher(id);
        AuthUser authUser = loadAuthUser(teacher.getProfile().getAuthUserId());
        authUser.setActive(false);
        authUserRepository.save(authUser);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private void validateUniqueAuthFields(String email, String username, String cccd) {
        if (authUserRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }
        if (authUserRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        if (authUserRepository.existsByCccd(cccd)) {
            throw new IllegalArgumentException("CCCD đã được sử dụng");
        }
    }

    private Student findStudent(UUID id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy học sinh"));
    }

    private Teacher findTeacher(UUID id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giáo viên"));
    }

    private AuthUser loadAuthUser(UUID authUserId) {
        return authUserRepository.findById(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
    }

    private StudentAdminResponse mapStudent(Student student, AuthUser authUser) {
        Profile profile = student.getProfile();
        return StudentAdminResponse.builder()
                .id(student.getId())
                .authUserId(authUser.getId())
                .profileId(profile.getId())
                .username(authUser.getUsername())
                .email(authUser.getEmail())
                .fullName(profile.getFullName())
                .phone(profile.getPhone())
                .studentCode(student.getStudentCode())
                .enrollmentDate(student.getEnrollmentDate())
                .active(authUser.isActive())
                .build();
    }

    private TeacherAdminResponse mapTeacher(Teacher teacher, AuthUser authUser) {
        Profile profile = teacher.getProfile();
        return TeacherAdminResponse.builder()
                .id(teacher.getId())
                .authUserId(authUser.getId())
                .profileId(profile.getId())
                .username(authUser.getUsername())
                .email(authUser.getEmail())
                .fullName(profile.getFullName())
                .phone(profile.getPhone())
                .teacherCode(teacher.getTeacherCode())
                .department(teacher.getDepartment())
                .hireDate(teacher.getHireDate())
                .active(authUser.isActive())
                .build();
    }
}
