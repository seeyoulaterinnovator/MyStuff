import type { AppRouteObject } from "../routes";
import {
  CustomSettingsRoute,
  CustomSettingsWithTab,
} from "./routes/CustomSettings";

const routes: AppRouteObject[] = [CustomSettingsRoute, CustomSettingsWithTab];

export default routes;
