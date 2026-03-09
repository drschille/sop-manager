import { Link, NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../auth/AuthProvider";

export function AppShell() {
  const auth = useAuth();

  return (
    <div className="app-shell">
      <header className="topbar">
        <Link to="/" className="brand">
          SOP Manager
        </Link>
        <nav className="nav">
          <NavLink to="/" end>
            Search
          </NavLink>
          <NavLink to="/parts/new">Create SOP</NavLink>
        </nav>
        <div className="user-box">
          <span>{auth.backendUser?.email ?? auth.profileEmail ?? "Unknown user"}</span>
          <button onClick={() => void auth.signOut()}>Sign out</button>
        </div>
      </header>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
