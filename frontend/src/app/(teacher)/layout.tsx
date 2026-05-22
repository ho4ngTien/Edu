import { Outlet } from 'react-router-dom';

export default function TeacherLayout() {
  return (
    <div className="teacher-layout">
      <header>Teacher Header</header>
      <div className="flex">
        <aside>Teacher Sidebar</aside>
        <main>
          <Outlet />
        </main>
      </div>
    </div>
  );
}
