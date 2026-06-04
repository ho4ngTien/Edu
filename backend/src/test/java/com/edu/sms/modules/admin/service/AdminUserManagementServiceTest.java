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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserManagementServiceTest {

    @Mock
    private AuthUserRepository authUserRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private TeacherRepository teacherRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminUserManagementService service;

    private Role studentRole;
    private Role teacherRole;
    private UUID authUserId;
    private UUID profileId;
    private UUID studentId;
    private UUID teacherId;

    @BeforeEach
    void setUp() {
        studentRole = Role.builder().id(UUID.randomUUID()).name("ROLE_STUDENT").build();
        teacherRole = Role.builder().id(UUID.randomUUID()).name("ROLE_TEACHER").build();
        authUserId = UUID.randomUUID();
        profileId = UUID.randomUUID();
        studentId = UUID.randomUUID();
        teacherId = UUID.randomUUID();
    }

    // ─── Students ─────────────────────────────────────────────────────────

    @Test
    void createStudent_Success() {
        StudentAdminRequest request = StudentAdminRequest.builder()
                .username("student1")
                .email("student1@sms.edu")
                .cccd("123456789012")
                .password("password123")
                .fullName("Nguyen Van Student")
                .phone("0987654321")
                .studentCode("ST001")
                .enrollmentDate(LocalDate.now())
                .build();

        when(authUserRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(authUserRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(authUserRepository.existsByCccd(request.getCccd())).thenReturn(false);
        when(studentRepository.existsByStudentCode(request.getStudentCode())).thenReturn(false);
        when(roleRepository.findByName("ROLE_STUDENT")).thenReturn(Optional.of(studentRole));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_password");

        AuthUser savedAuth = AuthUser.builder()
                .id(authUserId)
                .username(request.getUsername())
                .email(request.getEmail())
                .cccd(request.getCccd())
                .passwordHash("encoded_password")
                .roles(Set.of(studentRole))
                .isActive(true)
                .build();
        when(authUserRepository.save(any(AuthUser.class))).thenReturn(savedAuth);

        Profile savedProfile = Profile.builder()
                .id(profileId)
                .authUserId(authUserId)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .build();
        when(profileRepository.save(any(Profile.class))).thenReturn(savedProfile);

        Student savedStudent = Student.builder()
                .id(studentId)
                .profile(savedProfile)
                .studentCode(request.getStudentCode())
                .enrollmentDate(request.getEnrollmentDate())
                .build();
        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

        StudentAdminResponse response = service.createStudent(request);

        assertNotNull(response);
        assertEquals(studentId, response.getId());
        assertEquals(authUserId, response.getAuthUserId());
        assertEquals(profileId, response.getProfileId());
        assertEquals(request.getStudentCode(), response.getStudentCode());
        assertEquals(request.getFullName(), response.getFullName());
        assertTrue(response.isActive());

        verify(authUserRepository).save(any(AuthUser.class));
        verify(profileRepository).save(any(Profile.class));
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void createStudent_DuplicateEmail_ThrowsException() {
        StudentAdminRequest request = StudentAdminRequest.builder()
                .email("duplicate@sms.edu")
                .build();

        when(authUserRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.createStudent(request));
    }

    @Test
    void getStudent_NotFound_ThrowsException() {
        UUID id = UUID.randomUUID();
        when(studentRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getStudent(id));
    }

    @Test
    void deleteStudent_Success() {
        Student student = Student.builder()
                .id(studentId)
                .profile(Profile.builder().authUserId(authUserId).build())
                .build();
        AuthUser authUser = AuthUser.builder().id(authUserId).isActive(true).build();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(authUserRepository.findById(authUserId)).thenReturn(Optional.of(authUser));

        service.deleteStudent(studentId);

        assertFalse(authUser.isActive());
        verify(authUserRepository).save(authUser);
    }

    // ─── Teachers ─────────────────────────────────────────────────────────

    @Test
    void createTeacher_Success() {
        TeacherAdminRequest request = TeacherAdminRequest.builder()
                .username("teacher1")
                .email("teacher1@sms.edu")
                .cccd("987654321012")
                .password("password123")
                .fullName("Tran Thi Teacher")
                .phone("0912345678")
                .teacherCode("TC001")
                .department("Computer Science")
                .hireDate(LocalDate.now())
                .build();

        when(authUserRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(authUserRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(authUserRepository.existsByCccd(request.getCccd())).thenReturn(false);
        when(teacherRepository.existsByTeacherCode(request.getTeacherCode())).thenReturn(false);
        when(roleRepository.findByName("ROLE_TEACHER")).thenReturn(Optional.of(teacherRole));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_password");

        AuthUser savedAuth = AuthUser.builder()
                .id(authUserId)
                .username(request.getUsername())
                .email(request.getEmail())
                .cccd(request.getCccd())
                .passwordHash("encoded_password")
                .roles(Set.of(teacherRole))
                .isActive(true)
                .build();
        when(authUserRepository.save(any(AuthUser.class))).thenReturn(savedAuth);

        Profile savedProfile = Profile.builder()
                .id(profileId)
                .authUserId(authUserId)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .build();
        when(profileRepository.save(any(Profile.class))).thenReturn(savedProfile);

        Teacher savedTeacher = Teacher.builder()
                .id(teacherId)
                .profile(savedProfile)
                .teacherCode(request.getTeacherCode())
                .department(request.getDepartment())
                .hireDate(request.getHireDate())
                .build();
        when(teacherRepository.save(any(Teacher.class))).thenReturn(savedTeacher);

        TeacherAdminResponse response = service.createTeacher(request);

        assertNotNull(response);
        assertEquals(teacherId, response.getId());
        assertEquals(authUserId, response.getAuthUserId());
        assertEquals(profileId, response.getProfileId());
        assertEquals(request.getTeacherCode(), response.getTeacherCode());
        assertEquals(request.getDepartment(), response.getDepartment());
        assertTrue(response.isActive());
    }
}
