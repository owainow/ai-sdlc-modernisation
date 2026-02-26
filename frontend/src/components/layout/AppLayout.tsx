import { Outlet } from 'react-router-dom';
import Navigation from './Navigation';

export default function AppLayout() {
  return (
    <div className="min-h-screen bg-slate-50">
      <Navigation />
      <main className="max-w-7xl mx-auto px-4 py-6">
        <Outlet />
      </main>
    </div>
  );
}
