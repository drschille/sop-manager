import { useState } from "react";
import { useQuery } from "convex/react";
import { Link, useParams } from "react-router-dom";
import type { Id } from "../../../../convex/_generated/dataModel";
import { api } from "../../../../convex/_generated/api";

export function HistoryPage() {
  const { procedureId = "" } = useParams();

  if (!procedureId) {
    return (
      <div className="card">
        <h1>Invalid procedure</h1>
        <p className="muted">No procedure ID was provided.</p>
        <Link to="/">Back to search</Link>
      </div>
    );
  }

  const typedProcedureId = procedureId as Id<"procedures">;

  const versions = useQuery(api.procedures.listVersions, {
    procedureId: typedProcedureId,
  });

  const [selectedVersionId, setSelectedVersionId] = useState<Id<"procedureVersions"> | null>(null);

  const selectedVersion = useQuery(
    api.procedures.getVersion,
    selectedVersionId ? { versionId: selectedVersionId } : "skip",
  );

  if (!versions) {
    return <p>Loading history...</p>;
  }

  return (
    <section className="stack">
      <div className="card">
        <h1>Version history</h1>
        {!versions.length && <p>No versions found.</p>}
        {!!versions.length && (
          <ul className="result-list">
            {versions.map((version: { _id: Id<"procedureVersions">; versionNumber: number; createdAt: number; createdBy: string }) => (
              <li key={version._id}>
                <button type="button" onClick={() => setSelectedVersionId(version._id)}>
                  Version {version.versionNumber} | {new Date(version.createdAt).toLocaleString()} | {version.createdBy}
                </button>
              </li>
            ))}
          </ul>
        )}
        <Link to={`/procedures/${procedureId}/edit`}>Back to edit</Link>
      </div>

      {selectedVersion && (
        <div className="card stack">
          <h2>
            Version {selectedVersion.versionNumber} - {selectedVersion.title}
          </h2>
          <p className="preserve-lines">{selectedVersion.body}</p>
          <p className="muted">
            Edited by {selectedVersion.createdBy} at {new Date(selectedVersion.createdAt).toLocaleString()}
          </p>

          <h3>Photos</h3>
          {!selectedVersion.photos.length && <p className="muted">No photos in this version</p>}
          {!!selectedVersion.photos.length && (
            <div className="gallery">
              {selectedVersion.photos.map((photo: { storageId: string; url: string | null; description?: string }, index: number) => (
                <div key={photo.storageId} className="gallery-item">
                  {photo.url ? (
                    <img
                      src={photo.url}
                      alt={photo.description?.trim() || `Version ${selectedVersion.versionNumber} photo`}
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

          {selectedVersion.part?.partNumber && (
            <Link to={`/parts/${encodeURIComponent(selectedVersion.part.partNumber)}`}>Back to current SOP</Link>
          )}
        </div>
      )}
    </section>
  );
}
