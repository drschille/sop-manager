import { PublicClientApplication } from "@azure/msal-browser";

const tenantId = import.meta.env.VITE_ENTRA_TENANT_ID ?? "<ENTRA_TENANT_ID>";
const clientId = import.meta.env.VITE_ENTRA_CLIENT_ID ?? "<ENTRA_CLIENT_ID>";
const redirectUri = import.meta.env.VITE_ENTRA_REDIRECT_URI ?? window.location.origin;

const authority = `https://login.microsoftonline.com/${tenantId}`;

export const msal = new PublicClientApplication({
  auth: {
    authority,
    clientId,
    redirectUri,
  },
  cache: {
    cacheLocation: "localStorage",
    storeAuthStateInCookie: false,
  },
});

export async function initMsal() {
  await msal.initialize();
  const response = await msal.handleRedirectPromise();
  if (response?.account) {
    msal.setActiveAccount(response.account);
    return;
  }

  const active = msal.getActiveAccount() ?? msal.getAllAccounts()[0] ?? null;
  if (active) {
    msal.setActiveAccount(active);
  }
}

export function getActiveAccount() {
  return msal.getActiveAccount() ?? msal.getAllAccounts()[0] ?? null;
}

export async function signIn() {
  await msal.loginRedirect({
    scopes: ["openid", "profile", "email"],
  });
}

export async function signOut() {
  const active = getActiveAccount();
  await msal.logoutRedirect({
    account: active ?? undefined,
    postLogoutRedirectUri: window.location.origin,
  });
}

export async function getIdToken(): Promise<string | null> {
  const account = getActiveAccount();
  if (!account) {
    return null;
  }

  const token = await msal.acquireTokenSilent({
    account,
    scopes: ["openid", "profile", "email"],
  });
  return token.idToken;
}
