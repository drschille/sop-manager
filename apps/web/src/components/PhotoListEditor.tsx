import { useState } from "react";
import type { Id } from "../../../../convex/_generated/dataModel";

export type PhotoItem = {
  storageId: Id<"_storage">;
  url?: string | null;
  name?: string;
  description?: string;
};

export function PhotoListEditor({
  photos,
  onChange,
}: {
  photos: PhotoItem[];
  onChange: (next: PhotoItem[]) => void;
}) {
  const [draggingIndex, setDraggingIndex] = useState<number | null>(null);
  const [dragOverIndex, setDragOverIndex] = useState<number | null>(null);

  const move = (from: number, to: number) => {
    if (to < 0 || to >= photos.length) {
      return;
    }
    const clone = [...photos];
    const [item] = clone.splice(from, 1);
    clone.splice(to, 0, item);
    onChange(clone);
  };

  const remove = (index: number) => {
    const clone = [...photos];
    clone.splice(index, 1);
    onChange(clone);
  };

  if (!photos.length) {
    return <p className="muted">No photos selected</p>;
  }

  return (
    <ul className="photo-list">
      {photos.map((photo, index) => (
        <li
          key={`${photo.storageId}-${index}`}
          className={`photo-item${draggingIndex === index ? " photo-item-dragging" : ""}${dragOverIndex === index && draggingIndex !== index ? " photo-item-drag-over" : ""}`}
          onDragOver={(event) => {
            event.preventDefault();
            if (dragOverIndex !== index) {
              setDragOverIndex(index);
            }
          }}
          onDragLeave={() => {
            if (dragOverIndex === index) {
              setDragOverIndex(null);
            }
          }}
          onDrop={(event) => {
            event.preventDefault();
            if (draggingIndex === null || draggingIndex === index) {
              setDragOverIndex(null);
              return;
            }
            move(draggingIndex, index);
            setDraggingIndex(null);
            setDragOverIndex(null);
          }}
        >
          <button
            type="button"
            className="drag-handle-left"
            draggable
            onDragStart={() => setDraggingIndex(index)}
            onDragEnd={() => {
              setDraggingIndex(null);
              setDragOverIndex(null);
            }}
            aria-label={`Drag to reorder image ${index + 1}`}
            title="Drag to reorder"
          >
            <span className="burger-line" />
            <span className="burger-line" />
            <span className="burger-line" />
          </button>
          <div className="photo-preview">
            {photo.url ? <img src={photo.url} alt={photo.name ?? `SOP image ${index + 1}`} /> : <span>No preview</span>}
          </div>
          <div className="photo-actions">
            <span className="photo-name">{photo.name ?? `Image ${index + 1}`}</span>
            <label className="photo-description-label">
              Step instruction for this image (optional)
              <input
                value={photo.description ?? ""}
                placeholder="Explain what to do when viewing this image"
                onChange={(event) => {
                  const clone = [...photos];
                  clone[index] = {
                    ...clone[index],
                    description: event.target.value,
                  };
                  onChange(clone);
                }}
              />
            </label>
            <div className="photo-action-row">
              <button
                type="button"
                className="remove-photo-button"
                onClick={() => remove(index)}
                aria-label={`Remove image ${index + 1}`}
                title="Remove image"
              >
                x
              </button>
            </div>
          </div>
        </li>
      ))}
    </ul>
  );
}
