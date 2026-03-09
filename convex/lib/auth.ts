import type { QueryCtx, MutationCtx, ActionCtx } from "../_generated/server";

type Ctx = QueryCtx | MutationCtx | ActionCtx;

function getEnv(name: string): string | undefined {
  const maybeProcess = (globalThis as { process?: { env?: Record<string, string | undefined> } }).process;
  return maybeProcess?.env?.[name];
}

export async function requireUser(ctx: Ctx) {
  const identity = await ctx.auth.getUserIdentity();
  if (!identity) {
    throw new Error("Not authenticated");
  }
  return identity;
}

export async function requireTenant(ctx: Ctx) {
  const identity = await requireUser(ctx);
  const tenantId = getEnv("ENTRA_TENANT_ID");

  if (!tenantId) {
    // Local/dev fallback: when tenant ID is not configured, only require auth.
    return identity;
  }

  const expectedIssuer = `https://login.microsoftonline.com/${tenantId}/v2.0`;
  if (identity.issuer !== expectedIssuer) {
    throw new Error("User is not from the allowed Entra tenant");
  }

  return identity;
}

export function userLabel(identity: {
  email?: string;
  name?: string;
  subject: string;
}) {
  return identity.email ?? identity.name ?? identity.subject;
}
