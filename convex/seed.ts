import { mutation } from "./_generated/server";
import { v } from "convex/values";

// Seed helper for local/dev environments.
// Pass uploaded photo IDs if you want seeded procedures with actual photos.
export const seedSampleData = mutation({
  args: {
    samplePhotoIds: v.optional(v.array(v.id("_storage"))),
  },
  handler: async (ctx, args) => {
    const now = Date.now();
    const samplePhotoIds = args.samplePhotoIds ?? [];

    const existingPart = await ctx.db
      .query("parts")
      .withIndex("by_partNumber", (q) => q.eq("partNumber", "ABC-12345"))
      .unique();

    if (existingPart) {
      return { seeded: false, reason: "Sample data already exists" };
    }

    const partWithSopId = await ctx.db.insert("parts", {
      partNumber: "ABC-12345",
      qrValue: "myapp://part/ABC-12345",
      createdAt: now,
    });

    const partWithVersionsId = await ctx.db.insert("parts", {
      partNumber: "XYZ-20001",
      qrValue: "XYZ-20001",
      createdAt: now,
    });

    await ctx.db.insert("parts", {
      partNumber: "NOP-EMPTY-01",
      qrValue: "myapp://part/NOP-EMPTY-01",
      createdAt: now,
    });

    const proc1 = await ctx.db.insert("procedures", {
      partId: partWithSopId,
      currentVersionId: undefined,
      createdAt: now,
      createdBy: "seed@local",
    });

    const v1 = await ctx.db.insert("procedureVersions", {
      procedureId: proc1,
      title: "Assembly SOP",
      body: "1) Inspect label\n2) Align bracket\n3) Tighten to 6 Nm",
      photoStorageIds: samplePhotoIds.slice(0, 2),
      versionNumber: 1,
      createdAt: now,
      createdBy: "seed@local",
    });

    await ctx.db.patch(proc1, { currentVersionId: v1 });
    await ctx.db.insert("auditLog", {
      procedureId: proc1,
      versionId: v1,
      action: "created",
      performedBy: "seed@local",
      performedAt: now,
    });

    const proc2 = await ctx.db.insert("procedures", {
      partId: partWithVersionsId,
      currentVersionId: undefined,
      createdAt: now,
      createdBy: "seed@local",
    });

    const v2_1 = await ctx.db.insert("procedureVersions", {
      procedureId: proc2,
      title: "Packing SOP",
      body: "Use carton type A. Add 2 desiccant packs.",
      photoStorageIds: [],
      versionNumber: 1,
      createdAt: now - 86_400_000,
      createdBy: "seed@local",
    });

    const v2_2 = await ctx.db.insert("procedureVersions", {
      procedureId: proc2,
      title: "Packing SOP (Revised)",
      body: "Use carton type B for export. Add 3 desiccant packs.",
      photoStorageIds: samplePhotoIds.slice(0, 1),
      versionNumber: 2,
      createdAt: now,
      createdBy: "seed@local",
    });

    await ctx.db.patch(proc2, { currentVersionId: v2_2 });
    await ctx.db.insert("auditLog", {
      procedureId: proc2,
      versionId: v2_1,
      action: "created",
      performedBy: "seed@local",
      performedAt: now - 86_400_000,
    });
    await ctx.db.insert("auditLog", {
      procedureId: proc2,
      versionId: v2_2,
      action: "edited",
      performedBy: "seed@local",
      performedAt: now,
    });

    return { seeded: true };
  },
});
