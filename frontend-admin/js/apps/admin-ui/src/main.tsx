import "@patternfly/patternfly/patternfly-addons.css";
import "@patternfly/react-core/dist/styles/base.css";

import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { createHashRouter, RouterProvider } from "react-router-dom";
import { i18n } from "./i18n/i18n";
import { RootRoute } from "./routes";

import "./index.css";
import { HashInterceptor } from "./root/HashInterceptor";

// Initialize required components before rendering app.
await i18n.init();

const router = createHashRouter([RootRoute]);
const container = document.getElementById("app");
const root = createRoot(container!);

root.render(
  <StrictMode>
    <HashInterceptor>
      <RouterProvider router={router} />
    </HashInterceptor>
  </StrictMode>,
);
