import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { ConvexProvider } from "convex/react";
import App from "./App";
import { convex } from "./lib/convex";
import { AuthProvider } from "./auth/AuthProvider";
import "./styles.css";

createRoot(document.getElementById("root")!).render(
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
