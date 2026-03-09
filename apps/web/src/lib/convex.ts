import { ConvexReactClient } from "convex/react";

export const convexUrl = import.meta.env.VITE_CONVEX_URL ?? "";
export const mockAuthEnabled = import.meta.env.VITE_MOCK_AUTH === "true";

export const convex = convexUrl ? new ConvexReactClient(convexUrl) : null;
export const convexAvailable = !!convex;
