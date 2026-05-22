import { Outlet } from 'react-router-dom';

export default function StudentLayout() {
  return (
    <div className="student-layout">
      <header>Student Header</header>
      <div className="flex">
        <aside>Student Sidebar</aside>
        <main>
          <Outlet />
        </main>
      </div>
    </div>
  );
}
