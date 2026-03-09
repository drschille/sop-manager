import { mutation, query } from "./_generated/server";
import { v } from "convex/values";
import { requireTenant } from "./lib/auth";

export const generateUploadUrl = mutation({
  args: {},
  handler: async (ctx) => {
    await requireTenant(ctx);
    const uploadUrl = await ctx.storage.generateUploadUrl();
    return { uploadUrl };
  },
});

export const getPhotoUrl = query({
  args: {
    storageId: v.id("_storage"),
  },
  handler: async (ctx, args) => {
    await requireTenant(ctx);
    const url = await ctx.storage.getUrl(args.storageId);
    return { storageId: args.storageId, url };
  },
});
