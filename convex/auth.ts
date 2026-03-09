import { query } from "./_generated/server";
import { requireTenant } from "./lib/auth";

export const getCurrentUser = query({
  args: {},
  handler: async (ctx) => {
    const identity = await requireTenant(ctx);
    return {
      subject: identity.subject,
      name: identity.name ?? null,
      email: identity.email ?? null,
      issuer: identity.issuer,
    };
  },
});
