import type { Response } from "../jsonResponse.js";

export type CustomConfigRepresentation = {
  adminTheme?: string;
};

export interface CustomConfigResponseRepresentation
  extends Response<CustomConfigRepresentation> {}
