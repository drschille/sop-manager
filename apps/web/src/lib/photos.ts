import type { Id } from "../../../../convex/_generated/dataModel";

export async function uploadToConvexStorage(
  uploadUrl: string,
  file: File,
): Promise<Id<"_storage">> {
  const response = await fetch(uploadUrl, {
    method: "POST",
    headers: {
      "Content-Type": file.type || "application/octet-stream",
    },
    body: file,
  });

  if (!response.ok) {
    throw new Error(`Upload failed: ${response.status}`);
  }

  const body = (await response.json()) as { storageId: Id<"_storage"> };
  return body.storageId;
}
