import { v } from "convex/values";
import { mutation, query } from "./_generated/server";
import type { Id } from "./_generated/dataModel";
import { requireTenant, userLabel } from "./lib/auth";

async function resolvePhotoUrls(ctx: Parameters<typeof query>[0]["handler"] extends never ? never : any, storageIds: Id<"_storage">[]) {
  const urls = await Promise.all(storageIds.map((id) => ctx.storage.getUrl(id)));
  return storageIds.map((id, index) => ({
    storageId: id,
    url: urls[index],
  }));
}

export const getByPartNumber = query({
  args: {
    partNumber: v.string(),
  },
  handler: async (ctx, args) => {
    await requireTenant(ctx);

    const part = await ctx.db
      .query("parts")
      .withIndex("by_partNumber", (q) => q.eq("partNumber", args.partNumber))
      .unique();

    if (!part) {
      return {
        part: null,
        procedure: null,
        currentVersion: null,
      };
    }

    const procedure = await ctx.db
      .query("procedures")
      .withIndex("by_partId", (q) => q.eq("partId", part._id))
      .unique();

    if (!procedure || !procedure.currentVersionId) {
      return {
        part,
        procedure: procedure ?? null,
        currentVersion: null,
      };
    }

    const currentVersion = await ctx.db.get(procedure.currentVersionId);
    if (!currentVersion) {
      return {
        part,
        procedure,
        currentVersion: null,
      };
    }

    return {
      part,
      procedure,
      currentVersion: {
        ...currentVersion,
        photos: await resolvePhotoUrls(ctx, currentVersion.photoStorageIds),
      },
    };
  },
});

export const create = mutation({
  args: {
    partNumber: v.string(),
    title: v.string(),
    body: v.string(),
    photoIds: v.array(v.id("_storage")),
  },
  handler: async (ctx, args) => {
    const identity = await requireTenant(ctx);
    const actor = userLabel(identity);

    const normalizedPartNumber = args.partNumber.trim();
    if (!normalizedPartNumber) {
      throw new Error("partNumber is required");
    }

    let part = await ctx.db
      .query("parts")
      .withIndex("by_partNumber", (q) => q.eq("partNumber", normalizedPartNumber))
      .unique();

    if (!part) {
      const partId = await ctx.db.insert("parts", {
        partNumber: normalizedPartNumber,
        qrValue: undefined,
        createdAt: Date.now(),
      });
      part = await ctx.db.get(partId);
      if (!part) {
        throw new Error("Failed to create part");
      }
    }

    const existingProcedure = await ctx.db
      .query("procedures")
      .withIndex("by_partId", (q) => q.eq("partId", part._id))
      .unique();

    if (existingProcedure) {
      throw new Error("Procedure already exists for this part");
    }

    const procedureId = await ctx.db.insert("procedures", {
      partId: part._id,
      createdAt: Date.now(),
      createdBy: actor,
      currentVersionId: undefined,
    });

    const versionId = await ctx.db.insert("procedureVersions", {
      procedureId,
      title: args.title,
      body: args.body,
      photoStorageIds: args.photoIds,
      versionNumber: 1,
      createdAt: Date.now(),
      createdBy: actor,
    });

    await ctx.db.patch(procedureId, {
      currentVersionId: versionId,
    });

    await ctx.db.insert("auditLog", {
      procedureId,
      versionId,
      action: "created",
      performedBy: actor,
      performedAt: Date.now(),
    });

    return { procedureId, versionId, partId: part._id };
  },
});

export const edit = mutation({
  args: {
    procedureId: v.id("procedures"),
    title: v.string(),
    body: v.string(),
    photoIds: v.array(v.id("_storage")),
  },
  handler: async (ctx, args) => {
    const identity = await requireTenant(ctx);
    const actor = userLabel(identity);

    const procedure = await ctx.db.get(args.procedureId);
    if (!procedure) {
      throw new Error("Procedure not found");
    }

    const latestVersion = await ctx.db
      .query("procedureVersions")
      .withIndex("by_procedureId", (q) => q.eq("procedureId", args.procedureId))
      .order("desc")
      .first();

    const nextVersionNumber = (latestVersion?.versionNumber ?? 0) + 1;

    const versionId = await ctx.db.insert("procedureVersions", {
      procedureId: args.procedureId,
      title: args.title,
      body: args.body,
      photoStorageIds: args.photoIds,
      versionNumber: nextVersionNumber,
      createdAt: Date.now(),
      createdBy: actor,
    });

    await ctx.db.patch(args.procedureId, {
      currentVersionId: versionId,
    });

    await ctx.db.insert("auditLog", {
      procedureId: args.procedureId,
      versionId,
      action: "edited",
      performedBy: actor,
      performedAt: Date.now(),
    });

    return { versionId, versionNumber: nextVersionNumber };
  },
});

export const listVersions = query({
  args: {
    procedureId: v.id("procedures"),
  },
  handler: async (ctx, args) => {
    await requireTenant(ctx);

    const versions = await ctx.db
      .query("procedureVersions")
      .withIndex("by_procedureId", (q) => q.eq("procedureId", args.procedureId))
      .order("desc")
      .collect();

    return versions.map((version) => ({
      _id: version._id,
      versionNumber: version.versionNumber,
      createdAt: version.createdAt,
      createdBy: version.createdBy,
      title: version.title,
    }));
  },
});

export const getVersion = query({
  args: {
    versionId: v.id("procedureVersions"),
  },
  handler: async (ctx, args) => {
    await requireTenant(ctx);

    const version = await ctx.db.get(args.versionId);
    if (!version) {
      return null;
    }

    const procedure = await ctx.db.get(version.procedureId);
    const part = procedure ? await ctx.db.get(procedure.partId) : null;

    return {
      ...version,
      procedure,
      part,
      photos: await resolvePhotoUrls(ctx, version.photoStorageIds),
    };
  },
});
