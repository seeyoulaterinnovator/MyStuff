import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react-swc";
import { checker } from "vite-plugin-checker";
import { PluginOption } from "vite";
import * as fs from "fs";

const outDir =
  "../../../../keycloak-extension/src/main/resources/themes/keycloak.v2/admin/resources";

const custom: () => PluginOption = () => {
  return {
    name: "custom",
    buildStart() {
      if (fs.existsSync(outDir)) {
        // т.к. при перестроении не удаляется старые файлы, в т.ч. при опции emptyOutDir
        fs.rmSync(outDir, { recursive: true, force: true });
        console.log("output directory cleared");
      }
    },
  };
};

// https://vitejs.dev/config/
export default defineConfig({
  base: "",
  server: {
    origin: "http://localhost:5174",
    port: 5174,
  },
  build: {
    sourcemap: true,
    target: "esnext",
    modulePreload: false,
    cssMinify: "lightningcss",
    manifest: true,
    rollupOptions: {
      input: "src/main.tsx",
      external: ["react", "react/jsx-runtime", "react-dom"],
    },
    outDir,
    emptyOutDir: false,
  },
  plugins: [react(), checker({ typescript: true }), custom()],
  test: {
    watch: false,
    environment: "jsdom",
    server: {
      deps: {
        inline: [/@patternfly\/.*/],
      },
    },
  },
});
