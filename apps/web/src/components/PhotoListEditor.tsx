import type { Id } from "../../../../convex/_generated/dataModel";

export type PhotoItem = {
  storageId: Id<"_storage">;
  url?: string | null;
  name?: string;
};

export function PhotoListEditor({
  photos,
  onChange,
}: {
  photos: PhotoItem[];
  onChange: (next: PhotoItem[]) => void;
}) {
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
        <li key={`${photo.storageId}-${index}`} className="photo-item">
          <div className="photo-preview">
            {photo.url ? <img src={photo.url} alt={photo.name ?? "SOP photo"} /> : <span>No preview</span>}
          </div>
          <div className="photo-actions">
            <span>{photo.name ?? photo.storageId}</span>
            <div>
              <button type="button" onClick={() => move(index, index - 1)}>
                Up
              </button>
              <button type="button" onClick={() => move(index, index + 1)}>
                Down
              </button>
              <button type="button" onClick={() => remove(index)}>
                Remove
              </button>
            </div>
          </div>
        </li>
      ))}
    </ul>
  );
}
