import { createBrowserRouter, Navigate } from 'react-router-dom';

// Layouts (Tách biệt hoàn toàn theo Role và Chức năng)
import AdminLayout from './(admin)/layout';
import TeacherLayout from './(teacher)/layout';
import StudentLayout from './(student)/layout';
import CourseLayout from './(courses)/layout';

// Giả lập Pages (Sẽ tạo thật sau)
const LoginPage = () => <div>Trang Đăng Nhập (Login)</div>;
const AdminDashboard = () => <div>Admin Dashboard</div>;

const StudentDashboard = () => <div>Student Dashboard</div>;
const StudentAssignments = () => <div>Router Bài Tập: Nơi học sinh nộp bài (Upload file/link)</div>;

const TeacherDashboard = () => <div>Teacher Dashboard</div>;
const TeacherSubjectDetail = () => <div>Router Môn Học: Tùy chỉnh môn học, upload tài liệu, tạo bài tập</div>;

const CourseDetail = () => <div>Router Khóa Học: Xem chi tiết bài giảng, tài liệu môn học (Chế độ tập trung học tập)</div>;

export const router = createBrowserRouter([
  {
    path: '/',
    element: <Navigate to="/login" replace />,
  },
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/admin',
    element: <AdminLayout />,
    children: [
      { index: true, element: <AdminDashboard /> },
      // /admin/users, /admin/academic-years, vv...
    ],
  },
  {
    path: '/teacher',
    element: <TeacherLayout />,
    children: [
      { index: true, element: <TeacherDashboard /> },
      { path: 'subjects/:subjectId', element: <TeacherSubjectDetail /> }, // Giáo viên tùy chỉnh môn học
    ],
  },
  {
    path: '/student',
    element: <StudentLayout />,
    children: [
      { index: true, element: <StudentDashboard /> },
      { path: 'assignments', element: <StudentAssignments /> }, // Học sinh nộp bài tập
    ],
  },
  {
    path: '/courses',
    element: <CourseLayout />,
    children: [
      { path: ':courseId', element: <CourseDetail /> }, // Xem nội dung khóa học/môn học (dùng chung cho ai có quyền)
    ],
  },
]);
