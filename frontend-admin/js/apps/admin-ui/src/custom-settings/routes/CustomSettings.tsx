import type { AppRouteObject } from "../../routes";
import { lazy } from "react";
import type { Path } from "react-router-dom";
import { generateEncodedPath } from "../../utils/generateEncodedPath";

export type CustomSettingsTab =
  | "general"
  | "front"
  | "message"
  | "application"
  | "gateway";

export type CustomSettingsParams = {
  realm: string;
  tab?: CustomSettingsTab;
};

const CustomSettingsSection = lazy(() => import("../CustomSettingsSection"));

export const CustomSettingsRoute: AppRouteObject = {
  path: "/:realm/custom-settings",
  element: <CustomSettingsSection />,
  breadcrumb: (t) => t("titleCustomSettings"),
  handle: {
    access: "manage-realm",
  },
};

export const CustomSettingsWithTab: AppRouteObject = {
  ...CustomSettingsRoute,
  path: "/:realm/custom-settings/:tab",
};

export const toCustomSettings = (
  params: CustomSettingsParams,
): Partial<Path> => {
  const path = params.tab
    ? CustomSettingsWithTab.path
    : CustomSettingsRoute.path;

  return {
    pathname: generateEncodedPath(path, params),
  };
};
