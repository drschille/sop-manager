import { useEffect, useMemo, useState } from "react";
import { useMutation, useQuery } from "convex/react";
import { Link, useNavigate, useParams } from "react-router-dom";
import type { Id } from "../../../../convex/_generated/dataModel";
import { api } from "../../../../convex/_generated/api";
import { PhotoListEditor, type PhotoItem } from "../components/PhotoListEditor";
import { toErrorMessage } from "../lib/errors";
import { uploadToConvexStorage } from "../lib/photos";

export function EditSopPage() {
  const navigate = useNavigate();
  const { partNumber, procedureId } = useParams();

  const isCreate = !procedureId;
  const decodedPartNumber = partNumber ? decodeURIComponent(partNumber) : "";

  const procedure = useQuery(
    api.procedures.getById,
    procedureId ? { procedureId: procedureId as Id<"procedures"> } : "skip",
  );

  const existingPhotos: PhotoItem[] = useMemo(() => {
    if (!procedure?.currentVersion) {
      return [];
    }
    return procedure.currentVersion.photos.map((photo: { storageId: string; url: string | null }) => ({
      storageId: photo.storageId,
      url: photo.url,
    }));
  }, [procedure]);

  const [part, setPart] = useState(decodedPartNumber || "");
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [photos, setPhotos] = useState<PhotoItem[]>([]);
  const [isSaving, setIsSaving] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [initializedFromProcedure, setInitializedFromProcedure] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const createProcedure = useMutation(api.procedures.create);
  const editProcedure = useMutation(api.procedures.edit);
  const generateUploadUrl = useMutation(api.photos.generateUploadUrl);

  useEffect(() => {
    if (!isCreate && procedure && !initializedFromProcedure) {
      setPart(procedure.part?.partNumber ?? "");
      setTitle(procedure.currentVersion?.title ?? "");
      setBody(procedure.currentVersion?.body ?? "");
      setPhotos(existingPhotos);
      setInitializedFromProcedure(true);
    }
  }, [isCreate, procedure, existingPhotos, initializedFromProcedure]);

  useEffect(() => {
    return () => {
      for (const photo of photos) {
        if (photo.url?.startsWith("blob:")) {
          URL.revokeObjectURL(photo.url);
        }
      }
    };
  }, [photos]);

  if (!isCreate && !procedure) {
    return <p>Loading SOP...</p>;
  }

  if (!isCreate && procedure === null) {
    return (
      <section className="card stack">
        <h1>SOP not found</h1>
        <p className="muted">This procedure does not exist or is no longer available.</p>
        <Link to="/">Back to search</Link>
      </section>
    );
  }

  const onUpload = async (files: FileList | null) => {
    if (!files?.length) {
      return;
    }

    setError(null);
    setIsUploading(true);

    try {
      const next: PhotoItem[] = [];
      for (const file of Array.from(files)) {
        const upload = await generateUploadUrl({});
        const storageId = await uploadToConvexStorage(upload.uploadUrl, file);
        next.push({
          storageId,
          url: URL.createObjectURL(file),
          name: file.name,
        });
      }
      setPhotos((prev) => [...prev, ...next]);
    } catch (uploadError) {
      setError(toErrorMessage(uploadError, "Upload failed"));
    } finally {
      setIsUploading(false);
    }
  };

  const onSave = async () => {
    if (!part.trim()) {
      setError("Part number is required");
      return;
    }

    if (!title.trim()) {
      setError("Title is required");
      return;
    }

    setIsSaving(true);
    setError(null);

    try {
      if (isCreate) {
        await createProcedure({
          partNumber: part.trim(),
          title: title.trim(),
          body: body.trim(),
          photoIds: photos.map((photo) => photo.storageId),
        });
      } else {
        await editProcedure({
          procedureId: procedureId as Id<"procedures">,
          title: title.trim(),
          body: body.trim(),
          photoIds: photos.map((photo) => photo.storageId),
        });
      }

      navigate(`/parts/${encodeURIComponent(part.trim())}`);
    } catch (saveError) {
      setError(toErrorMessage(saveError, "Save failed"));
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <section className="card stack">
      <h1>{isCreate ? "Create SOP" : "Edit SOP"}</h1>

      <label>
        Part number
        <input value={part} onChange={(e) => setPart(e.target.value)} disabled={!isCreate || isSaving} />
      </label>

      <label>
        Title
        <input value={title} onChange={(e) => setTitle(e.target.value)} disabled={isSaving} />
      </label>

      <label>
        SOP Text
        <textarea rows={8} value={body} onChange={(e) => setBody(e.target.value)} disabled={isSaving} />
      </label>

      <label>
        Add photos
        <input
          type="file"
          multiple
          accept="image/*"
          disabled={isSaving || isUploading}
          onChange={(e) => void onUpload(e.target.files)}
        />
      </label>

      {isUploading && <p className="muted">Uploading photos...</p>}
      <PhotoListEditor photos={photos} onChange={setPhotos} />

      {error && <p className="error">{error}</p>}

      <div className="inline-actions">
        <button disabled={isSaving || isUploading} onClick={() => void onSave()}>
          {isSaving ? "Saving..." : "Save"}
        </button>
        <Link to={part ? `/parts/${encodeURIComponent(part)}` : "/"}>Cancel</Link>
      </div>
    </section>
  );
}
