import { StrictMode } from "react";
import { createRoot } from "react-dom/client";

import { Toaster } from "sonner";

import "./index.css";
import App from "./App.tsx";

createRoot(
  document.getElementById("root")!
).render(
  <StrictMode>
    <App />

    <Toaster
      position="top-right"
      duration={4000}
      visibleToasts={3}
      offset={{
        top: 80,
        right: 24,
      }}
      mobileOffset={{
        top: 72,
        right: 16,
        left: 16,
      }}
      richColors
    />
  </StrictMode>
);