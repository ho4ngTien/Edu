import { createBrowserRouter, Navigate } from 'react-router-dom';

// Layouts (tách biệt theo role)
import AdminLayout from './(admin)/layout';
import TeacherLayout from './(teacher)/layout';
import StudentLayout from './(student)/layout';
import CourseLayout from './(courses)/layout';
import AuthLoginLayout from './(auth)/login/layout';

// Pages
import LoginPage from './(auth)/login/page';
import AdminDashboardPage from './(admin)/pages/DashboardPage';
import AdminStudentsPage from './(admin)/pages/StudentsPage';
import AdminTeachersPage from './(admin)/pages/TeachersPage';
import AdminSemestersPage from './(admin)/pages/SemestersPage';
import AdminCoursesPage from './(admin)/pages/CoursesPage';
import AdminClassesPage from './(admin)/pages/ClassesPage';
import TeacherDashboardPage from './(teacher)/pages/DashboardPage';
import TeacherClassesPage from './(teacher)/pages/ClassesPage';
import TeacherClassDetailPage from './(teacher)/pages/ClassDetailPage';
import TeacherSubjectDetailPage from './(teacher)/pages/SubjectDetailPage';
import TeacherAttendancePage from './(teacher)/pages/AttendancePage';
import TeacherGradeEntryPage from './(teacher)/pages/GradeEntryPage';
import StudentDashboardPage from './(student)/pages/DashboardPage';
import StudentAssignmentsPage from './(student)/pages/AssignmentsPage';
import StudentGradesPage from './(student)/pages/GradesPage';
import StudentSchedulePage from './(student)/pages/SchedulePage';
import CourseDetailPage from './(courses)/pages/CourseDetailPage';
import ProfilePage from './profile/page';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Navigate to="/login" replace />,
  },
  {
    path: '/login',
    element: <AuthLoginLayout />,
    children: [{ index: true, element: <LoginPage /> }],
  },
  {
    path: '/admin',
    element: <AdminLayout />,
    children: [
      { index: true, element: <AdminDashboardPage /> },
      { path: 'students', element: <AdminStudentsPage /> },
      { path: 'teachers', element: <AdminTeachersPage /> },
      { path: 'semesters', element: <AdminSemestersPage /> },
      { path: 'courses', element: <AdminCoursesPage /> },
      { path: 'classes', element: <AdminClassesPage /> },
    ],
  },
  {
    path: '/teacher',
    element: <TeacherLayout />,
    children: [
      { index: true, element: <TeacherDashboardPage /> },
      { path: 'classes', element: <TeacherClassesPage /> },
      { path: 'classes/:classId', element: <TeacherClassDetailPage /> },
      { path: 'subjects/:subjectId', element: <TeacherSubjectDetailPage /> },
      { path: 'attendance/:classId', element: <TeacherAttendancePage /> },
      { path: 'grades/:classId', element: <TeacherGradeEntryPage /> },
    ],
  },
  {
    path: '/student',
    element: <StudentLayout />,
    children: [
      { index: true, element: <StudentDashboardPage /> },
      { path: 'assignments', element: <StudentAssignmentsPage /> },
      { path: 'grades', element: <StudentGradesPage /> },
      { path: 'schedule', element: <StudentSchedulePage /> },
    ],
  },
  {
    path: '/courses',
    element: <CourseLayout />,
    children: [{ path: ':courseId', element: <CourseDetailPage /> }],
  },
  {
    path: '/profile',
    element: <ProfilePage />,
  },
]);
