import { useQuery } from "convex/react";
import { Link, useParams } from "react-router-dom";
import { api } from "../../../../convex/_generated/api";

export function SopDetailPage() {
  const { partNumber = "" } = useParams();
  const decodedPartNumber = decodeURIComponent(partNumber);

  if (!decodedPartNumber.trim()) {
    return (
      <div className="card">
        <h1>Invalid part</h1>
        <p className="muted">No part number was provided.</p>
        <Link to="/">Back to search</Link>
      </div>
    );
  }

  const data = useQuery(api.procedures.getByPartNumber, {
    partNumber: decodedPartNumber,
  });

  if (!data) {
    return <p>Loading SOP...</p>;
  }

  if (!data.part) {
    return (
      <div className="card">
        <h1>{decodedPartNumber}</h1>
        <p>Part not found.</p>
        <Link to={`/parts/${encodeURIComponent(decodedPartNumber)}/new`}>Create SOP</Link>
      </div>
    );
  }

  if (!data.currentVersion || !data.procedure) {
    return (
      <div className="card">
        <h1>{data.part.partNumber}</h1>
        <p>No SOP yet for this part.</p>
        <Link to={`/parts/${encodeURIComponent(data.part.partNumber)}/new`}>Create SOP</Link>
      </div>
    );
  }

  return (
    <section className="stack">
      <div className="card">
        <h1>{data.part.partNumber}</h1>
        <h2>{data.currentVersion.title}</h2>
        <p className="preserve-lines">{data.currentVersion.body}</p>
        <p className="muted">
          Last updated by {data.currentVersion.createdBy} at {new Date(data.currentVersion.createdAt).toLocaleString()}
        </p>
        <div className="inline-actions">
          <Link to={`/procedures/${data.procedure._id}/edit`}>Edit SOP</Link>
          <Link to={`/procedures/${data.procedure._id}/history`}>View history</Link>
        </div>
      </div>

      <div className="card">
        <h2>Photos</h2>
        {!data.currentVersion.photos.length && <p className="muted">No photos</p>}
        {!!data.currentVersion.photos.length && (
          <div className="gallery">
            {data.currentVersion.photos.map((photo: { storageId: string; url: string | null; description?: string }, index: number) => (
              <div key={photo.storageId} className="gallery-item">
                {photo.url ? (
                  <img
                    src={photo.url}
                    alt={photo.description?.trim() || `${data.currentVersion.title} photo`}
                  />
                ) : (
                  <span>Missing photo</span>
                )}
                {photo.description?.trim() && (
                  <div className="photo-instruction">
                    <h3>Step {index + 1}</h3>
                    <p className="preserve-lines">{photo.description}</p>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </section>
  );
}
