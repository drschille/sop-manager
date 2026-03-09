import { useMemo, useState } from "react";
import { useMutation, useQuery } from "convex/react";
import { Link, useNavigate, useParams } from "react-router-dom";
import type { Id } from "../../../../convex/_generated/dataModel";
import { api } from "../../../../convex/_generated/api";
import { PhotoListEditor, type PhotoItem } from "../components/PhotoListEditor";

async function uploadToConvex(uploadUrl: string, file: File): Promise<Id<"_storage">> {
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

export function EditSopPage() {
  const navigate = useNavigate();
  const { partNumber, procedureId } = useParams();

  const isCreate = !procedureId;
  const decodedPartNumber = partNumber ? decodeURIComponent(partNumber) : "";

  const procedure = useQuery(
    api.procedures.getById,
    procedureId ? { procedureId: procedureId as Id<"procedures"> } : "skip",
  );

  const defaultPhotos: PhotoItem[] = useMemo(() => {
    if (!procedure?.currentVersion) {
      return [];
    }
    return procedure.currentVersion.photos.map((photo) => ({
      storageId: photo.storageId,
      url: photo.url,
    }));
  }, [procedure]);

  const [part, setPart] = useState(decodedPartNumber || "");
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [photos, setPhotos] = useState<PhotoItem[]>([]);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const createProcedure = useMutation(api.procedures.create);
  const editProcedure = useMutation(api.procedures.edit);
  const generateUploadUrl = useMutation(api.photos.generateUploadUrl);
  const getPhotoUrl = useQuery;

  if (!isCreate && !procedure) {
    return <p>Loading...</p>;
  }

  if (!isCreate && procedure && title === "" && body === "" && photos.length === 0) {
    setTitle(procedure.currentVersion?.title ?? "");
    setBody(procedure.currentVersion?.body ?? "");
    setPhotos(defaultPhotos);
  }

  const onUpload = async (files: FileList | null) => {
    if (!files?.length) {
      return;
    }

    setError(null);

    try {
      const next: PhotoItem[] = [];
      for (const file of Array.from(files)) {
        const upload = await generateUploadUrl({});
        const storageId = await uploadToConvex(upload.uploadUrl, file);
        const photo = await getPhotoUrl(api.photos.getPhotoUrl, { storageId });
        next.push({ storageId, url: photo?.url ?? null, name: file.name });
      }
      setPhotos((prev) => [...prev, ...next]);
    } catch (uploadError) {
      setError(uploadError instanceof Error ? uploadError.message : "Upload failed");
    }
  };

  const onSave = async () => {
    if (!part.trim()) {
      setError("Part number is required");
      return;
    }

    setIsSaving(true);
    setError(null);

    try {
      if (isCreate) {
        await createProcedure({
          partNumber: part.trim(),
          title,
          body,
          photoIds: photos.map((photo) => photo.storageId),
        });
      } else {
        await editProcedure({
          procedureId: procedureId as Id<"procedures">,
          title,
          body,
          photoIds: photos.map((photo) => photo.storageId),
        });
      }

      navigate(`/parts/${encodeURIComponent(part.trim())}`);
    } catch (saveError) {
      setError(saveError instanceof Error ? saveError.message : "Save failed");
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <section className="card stack">
      <h1>{isCreate ? "Create SOP" : "Edit SOP"}</h1>

      <label>
        Part number
        <input value={part} onChange={(e) => setPart(e.target.value)} disabled={!isCreate} />
      </label>

      <label>
        Title
        <input value={title} onChange={(e) => setTitle(e.target.value)} />
      </label>

      <label>
        SOP Text
        <textarea rows={8} value={body} onChange={(e) => setBody(e.target.value)} />
      </label>

      <label>
        Add photos
        <input type="file" multiple accept="image/*" onChange={(e) => void onUpload(e.target.files)} />
      </label>

      <PhotoListEditor photos={photos} onChange={setPhotos} />

      {error && <p className="error">{error}</p>}

      <div className="inline-actions">
        <button disabled={isSaving} onClick={() => void onSave()}>
          {isSaving ? "Saving..." : "Save"}
        </button>
        <Link to={part ? `/parts/${encodeURIComponent(part)}` : "/"}>Cancel</Link>
      </div>
    </section>
  );
}
