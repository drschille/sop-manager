import { useState } from "react";
import { useQuery } from "convex/react";
import { Link } from "react-router-dom";
import { api } from "../../../../convex/_generated/api";

export function SearchPage() {
  const [query, setQuery] = useState("");
  const results = useQuery(api.parts.search, { partNumberQuery: query });

  return (
    <section className="stack">
      <div className="card">
        <h1>Search SOPs</h1>
        <input
          type="search"
          value={query}
          placeholder="Enter part number"
          onChange={(e) => setQuery(e.target.value)}
        />
      </div>

      <div className="card">
        <h2>Results</h2>
        {!results && <p>Loading...</p>}
        {results && results.length === 0 && <p>No matching parts</p>}
        {results && results.length > 0 && (
          <ul className="result-list">
            {results.map((part: { _id: string; partNumber: string; sopTitle: string | null; thumbnailUrl: string | null }) => (
              <li key={part._id} className="search-result-card">
                <Link to={`/parts/${encodeURIComponent(part.partNumber)}`} className="search-result-link">
                  <div className="search-thumb-wrap">
                    {part.thumbnailUrl ? (
                      <img
                        className="search-thumb"
                        src={part.thumbnailUrl}
                        alt={`${part.partNumber} thumbnail`}
                      />
                    ) : (
                      <div className="search-thumb-placeholder">No Image</div>
                    )}
                  </div>
                  <div className="search-result-text">
                    <h3>{part.partNumber}</h3>
                    <p>{part.sopTitle ?? "No SOP title yet"}</p>
                  </div>
                </Link>
              </li>
            ))}
          </ul>
        )}
        {query && (
          <Link className="button-link" to={`/parts/${encodeURIComponent(query)}/new`}>
            Create SOP for {query}
          </Link>
        )}
      </div>
    </section>
  );
}
