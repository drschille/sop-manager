import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import { ConvexProvider } from "convex/react";
import { convex } from "./lib/convex";
import { AuthProvider } from "./auth/AuthProvider";
import "./styles.css";

const root = document.getElementById("root");

if (!root) {
  throw new Error("Missing root element");
}

if (!convex) {
  createRoot(root).render(
    <StrictMode>
      <div className="card narrow">
        <h1>Missing Convex configuration</h1>
        <p>Add <code>VITE_CONVEX_URL</code> to <code>apps/web/.env.local</code> and restart the dev server.</p>
      </div>
    </StrictMode>,
  );
} else {
  createRoot(root).render(
    <StrictMode>
      <ConvexProvider client={convex}>
        <AuthProvider>
          <BrowserRouter>
            <App />
          </BrowserRouter>
        </AuthProvider>
      </ConvexProvider>
    </StrictMode>,
  );
}
