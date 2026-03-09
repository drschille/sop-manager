import { useAuth } from "../auth/AuthProvider";

export function LoginPage() {
  const auth = useAuth();

  return (
    <div className="card narrow">
      <h1>Sign in</h1>
      <p>Use your Microsoft Entra account to access SOP Manager.</p>
      {auth.authError && <p className="error">{auth.authError}</p>}
      <button onClick={() => void auth.signIn()}>Sign in with Microsoft</button>
    </div>
  );
}
