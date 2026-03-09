import { useState } from "react";
import { useQuery } from "convex/react";
import { Link, useParams } from "react-router-dom";
import type { Id } from "../../../../convex/_generated/dataModel";
import { api } from "../../../../convex/_generated/api";

export function HistoryPage() {
  const { procedureId = "" } = useParams();
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
            {versions.map((version) => (
              <li key={version._id}>
                <button onClick={() => setSelectedVersionId(version._id)}>
                  Version {version.versionNumber} | {new Date(version.createdAt).toLocaleString()} | {version.createdBy}
                </button>
              </li>
            ))}
          </ul>
        )}
        <Link to={`/procedures/${procedureId}/edit`}>Back to edit</Link>
      </div>

      {selectedVersion && (
        <div className="card">
          <h2>
            Version {selectedVersion.versionNumber} - {selectedVersion.title}
          </h2>
          <p className="preserve-lines">{selectedVersion.body}</p>
          <p className="muted">Read-only historical version</p>
        </div>
      )}
    </section>
  );
}
