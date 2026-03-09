import { query } from "./_generated/server";
import { v } from "convex/values";
import type { Doc } from "./_generated/dataModel";
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

    const hydratePart = async (part: Doc<"parts">) => {
      const procedure = await ctx.db
        .query("procedures")
        .withIndex("by_partId", (q) => q.eq("partId", part._id))
        .unique();

      if (!procedure?.currentVersionId) {
        return {
          ...part,
          sopTitle: null,
          thumbnailUrl: null,
        };
      }

      const currentVersion = await ctx.db.get(procedure.currentVersionId);
      if (!currentVersion) {
        return {
          ...part,
          sopTitle: null,
          thumbnailUrl: null,
        };
      }

      const firstPhotoId = currentVersion.photoStorageIds[0];
      const thumbnailUrl = firstPhotoId ? await ctx.storage.getUrl(firstPhotoId) : null;

      return {
        ...part,
        sopTitle: currentVersion.title,
        thumbnailUrl,
      };
    };

    if (!normalized) {
      const parts = await ctx.db.query("parts").order("desc").take(25);
      return await Promise.all(parts.map(hydratePart));
    }

    const parts = await ctx.db
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

    return await Promise.all(parts.map(hydratePart));
  },
});
