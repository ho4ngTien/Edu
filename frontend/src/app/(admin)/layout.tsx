import { Outlet } from 'react-router-dom';

export default function AdminLayout() {
  return (
    <div className="admin-layout">
      <header>Admin Header</header>
      <div className="flex">
        <aside>Admin Sidebar</aside>
        <main>
          <Outlet />
        </main>
      </div>
    </div>
  );
}
