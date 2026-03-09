import { httpRouter } from "convex/server";
import { httpAction } from "./_generated/server";
import { api } from "./_generated/api";
import type { Id } from "./_generated/dataModel";

const http = httpRouter();

function json(data: unknown, init?: ResponseInit) {
  return new Response(JSON.stringify(data), {
    ...init,
    headers: {
      "content-type": "application/json",
      ...(init?.headers ?? {}),
    },
  });
}

function badRequest(message: string) {
  return json({ error: message }, { status: 400 });
}

http.route({
  path: "/mobile/search",
  method: "GET",
  handler: httpAction(async (ctx, request) => {
    const url = new URL(request.url);
    const query = url.searchParams.get("query") ?? "";
    const results = await ctx.runQuery(api.parts.search, { partNumberQuery: query });
    return json({ results });
  }),
});

http.route({
  path: "/mobile/procedureByPart",
  method: "GET",
  handler: httpAction(async (ctx, request) => {
    const url = new URL(request.url);
    const partNumber = url.searchParams.get("partNumber")?.trim() ?? "";
    if (!partNumber) {
      return badRequest("partNumber is required");
    }
    const procedure = await ctx.runQuery(api.procedures.getByPartNumber, { partNumber });
    return json(procedure);
  }),
});

http.route({
  path: "/mobile/versions",
  method: "GET",
  handler: httpAction(async (ctx, request) => {
    const url = new URL(request.url);
    const procedureId = url.searchParams.get("procedureId")?.trim() ?? "";
    if (!procedureId) {
      return badRequest("procedureId is required");
    }
    const versions = await ctx.runQuery(api.procedures.listVersions, {
      procedureId: procedureId as Id<"procedures">,
    });
    return json({ versions });
  }),
});

http.route({
  path: "/mobile/version",
  method: "GET",
  handler: httpAction(async (ctx, request) => {
    const url = new URL(request.url);
    const versionId = url.searchParams.get("versionId")?.trim() ?? "";
    if (!versionId) {
      return badRequest("versionId is required");
    }
    const version = await ctx.runQuery(api.procedures.getVersion, {
      versionId: versionId as Id<"procedureVersions">,
    });
    return json(version);
  }),
});

http.route({
  path: "/mobile/create",
  method: "POST",
  handler: httpAction(async (ctx, request) => {
    const body = await request.json();
    const payload = body as {
      partNumber?: string;
      title?: string;
      body?: string;
      photos?: Array<{ storageId: string; description?: string }>;
    };
    if (!payload.partNumber?.trim()) {
      return badRequest("partNumber is required");
    }
    if (!payload.title?.trim()) {
      return badRequest("title is required");
    }
    const result = await ctx.runMutation(api.procedures.create, {
      partNumber: payload.partNumber.trim(),
      title: payload.title.trim(),
      body: payload.body?.trim() ?? "",
      photos: (payload.photos ?? []).map((photo) => ({
        storageId: photo.storageId as Id<"_storage">,
        description: photo.description?.trim() || undefined,
      })),
    });
    return json(result);
  }),
});

http.route({
  path: "/mobile/edit",
  method: "POST",
  handler: httpAction(async (ctx, request) => {
    const body = await request.json();
    const payload = body as {
      procedureId?: string;
      title?: string;
      body?: string;
      photos?: Array<{ storageId: string; description?: string }>;
    };
    if (!payload.procedureId?.trim()) {
      return badRequest("procedureId is required");
    }
    if (!payload.title?.trim()) {
      return badRequest("title is required");
    }
    const result = await ctx.runMutation(api.procedures.edit, {
      procedureId: payload.procedureId as Id<"procedures">,
      title: payload.title.trim(),
      body: payload.body?.trim() ?? "",
      photos: (payload.photos ?? []).map((photo) => ({
        storageId: photo.storageId as Id<"_storage">,
        description: photo.description?.trim() || undefined,
      })),
    });
    return json(result);
  }),
});

http.route({
  path: "/mobile/currentUser",
  method: "GET",
  handler: httpAction(async (ctx) => {
    const user = await ctx.runQuery(api.auth.getCurrentUser, {});
    return json(user);
  }),
});

export default http;
