import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { useQuery } from "convex/react";
import { api } from "../../../../convex/_generated/api";
import { convex, mockAuthEnabled } from "../lib/convex";
type MsalModule = typeof import("./msal");

type AuthContextValue = {
  initialized: boolean;
  isAuthenticated: boolean;
  authError: string | null;
  profileName: string | null;
  profileEmail: string | null;
  backendUser:
    | {
        subject: string;
        name: string | null;
        email: string | null;
        issuer: string;
      }
    | null
    | undefined;
  signIn: () => Promise<void>;
  signOut: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [initialized, setInitialized] = useState(false);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [authError, setAuthError] = useState<string | null>(null);
  const [profileName, setProfileName] = useState<string | null>(null);
  const [profileEmail, setProfileEmail] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function start() {
      try {
        if (mockAuthEnabled) {
          setProfileName("Mock User");
          setProfileEmail("mock@example.com");
          setIsAuthenticated(true);
          setAuthError(null);
          setInitialized(true);
          return;
        }

        const msalModule: MsalModule = await import("./msal");
        await msalModule.initMsal();
        const account = msalModule.getActiveAccount();

        if (account) {
          setProfileName(account.name ?? null);
          setProfileEmail(account.username ?? null);
          convex?.setAuth(async () => {
            try {
              return await msalModule.getIdToken();
            } catch (error) {
              setAuthError(error instanceof Error ? error.message : "Authentication failed");
              return null;
            }
          }, (authed) => {
            if (!cancelled) {
              setIsAuthenticated(authed);
              if (authed) {
                setAuthError(null);
              }
            }
          });
        } else {
          convex?.clearAuth();
          setIsAuthenticated(false);
        }
      } catch (error) {
        setAuthError(error instanceof Error ? error.message : "Failed to initialize authentication");
      } finally {
        if (!cancelled) {
          setInitialized(true);
        }
      }
    }

    void start();
    return () => {
      cancelled = true;
    };
  }, []);

  const backendUser = useQuery(
    api.auth.getCurrentUser,
    initialized && isAuthenticated ? {} : "skip",
  );

  const value = useMemo<AuthContextValue>(
    () => ({
      initialized,
      isAuthenticated,
      authError,
      profileName,
      profileEmail,
      backendUser,
      signIn: async () => {
        setAuthError(null);
        const msalModule: MsalModule = await import("./msal");
        await msalModule.signIn();
      },
      signOut: async () => {
        convex?.clearAuth();
        setAuthError(null);
        const msalModule: MsalModule = await import("./msal");
        await msalModule.signOut();
      },
    }),
    [initialized, isAuthenticated, authError, profileName, profileEmail, backendUser],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return ctx;
}
