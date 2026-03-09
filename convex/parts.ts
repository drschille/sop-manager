import { query } from "./_generated/server";
import { v } from "convex/values";
import { requireTenant } from "./lib/auth";

export const findByQrOrPartNumber = query({
  args: {
    qrValueOrPartNumber: v.string(),
  },
  handler: async (ctx, args) => {
    await requireTenant(ctx);

    const exactPart = await ctx.db
      .query("parts")
      .withIndex("by_partNumber", (q) => q.eq("partNumber", args.qrValueOrPartNumber))
      .unique();

    if (exactPart) {
      return exactPart;
    }

    const qrMatches = await ctx.db
      .query("parts")
      .filter((q) => q.eq(q.field("qrValue"), args.qrValueOrPartNumber))
      .take(1);

    return qrMatches[0] ?? null;
  },
});

export const search = query({
  args: {
    partNumberQuery: v.string(),
  },
  handler: async (ctx, args) => {
    await requireTenant(ctx);
    const normalized = args.partNumberQuery.trim().toLowerCase();

    if (!normalized) {
      return await ctx.db.query("parts").order("desc").take(25);
    }

    return await ctx.db
      .query("parts")
      .filter((q) =>
        q.gte(
          q.field("partNumber"),
          args.partNumberQuery.trim(),
        ),
      )
      .take(100)
      .then((rows) =>
        rows
          .filter((row) => row.partNumber.toLowerCase().includes(normalized))
          .slice(0, 25),
      );
  },
});
