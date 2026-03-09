import { Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "./auth/AuthProvider";
import { AppShell } from "./components/AppShell";
import { EditSopPage } from "./pages/EditSopPage";
import { HistoryPage } from "./pages/HistoryPage";
import { LoginPage } from "./pages/LoginPage";
import { SearchPage } from "./pages/SearchPage";
import { SopDetailPage } from "./pages/SopDetailPage";

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const auth = useAuth();
  if (!auth.isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}

function LoginRoute() {
  const auth = useAuth();
  if (auth.isAuthenticated) {
    return <Navigate to="/" replace />;
  }
  return <LoginPage />;
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginRoute />} />
      <Route
        element={(
          <ProtectedRoute>
            <AppShell />
          </ProtectedRoute>
        )}
      >
        <Route path="/" element={<SearchPage />} />
        <Route path="/parts/new" element={<EditSopPage />} />
        <Route path="/parts/:partNumber" element={<SopDetailPage />} />
        <Route path="/parts/:partNumber/new" element={<EditSopPage />} />
        <Route path="/procedures/:procedureId/edit" element={<EditSopPage />} />
        <Route path="/procedures/:procedureId/history" element={<HistoryPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default function App() {
  const auth = useAuth();

  if (!auth.initialized) {
    return <p className="centered">Loading authentication...</p>;
  }

  return <AppRoutes />;
}
