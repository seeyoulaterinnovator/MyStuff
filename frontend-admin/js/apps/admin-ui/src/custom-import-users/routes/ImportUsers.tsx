import { lazy } from "react";
import { generateEncodedPath } from "../../utils/generateEncodedPath";
import type { Path } from "react-router-dom";
import type { AppRouteObject } from "../../routes";

export type ImportUsersParams = { realm: string };

const ImportUsers = lazy(() => import("../ImportUsersSection"));

export const ImportUsersRoute: AppRouteObject = {
  path: "/:realm/import-users",
  element: <ImportUsers />,
  breadcrumb: (t) => t("titleUsers"),
  handle: {
    access: "query-users",
  },
};

export const toImportUsers = (params: ImportUsersParams): Partial<Path> => {
  const { path } = ImportUsersRoute;

  return {
    pathname: generateEncodedPath(path, params),
  };
};
