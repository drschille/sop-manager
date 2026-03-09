import { defineSchema, defineTable } from "convex/server";
import { v } from "convex/values";

export default defineSchema({
  parts: defineTable({
    partNumber: v.string(),
    qrValue: v.optional(v.string()),
    createdAt: v.number(),
  }).index("by_partNumber", ["partNumber"]),

  procedures: defineTable({
    partId: v.id("parts"),
    currentVersionId: v.optional(v.id("procedureVersions")),
    createdAt: v.number(),
    createdBy: v.string(),
  }).index("by_partId", ["partId"]),

  procedureVersions: defineTable({
    procedureId: v.id("procedures"),
    title: v.string(),
    body: v.string(),
    photoStorageIds: v.array(v.id("_storage")),
    photoDescriptions: v.optional(v.array(v.string())),
    versionNumber: v.number(),
    createdAt: v.number(),
    createdBy: v.string(),
  })
    .index("by_procedureId", ["procedureId"])
    .index("by_procedureId_versionNumber", ["procedureId", "versionNumber"]),

  auditLog: defineTable({
    procedureId: v.id("procedures"),
    versionId: v.id("procedureVersions"),
    action: v.union(v.literal("created"), v.literal("edited")),
    performedBy: v.string(),
    performedAt: v.number(),
  }).index("by_procedureId", ["procedureId"]),
});
