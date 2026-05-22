import { Outlet } from 'react-router-dom';

export default function CourseLayout() {
  return (
    <div className="course-layout">
      <header>Course Viewer Header (Chế độ tập trung học tập)</header>
      <main>
        <Outlet />
      </main>
    </div>
  );
}
