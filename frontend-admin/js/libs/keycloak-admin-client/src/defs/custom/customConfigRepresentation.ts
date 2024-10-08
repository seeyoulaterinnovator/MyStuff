import type { Response } from "../jsonResponse.js";

export type CustomConfigRepresentation = {
  adminTheme?: string;
};

export type CustomConfigResponseRepresentation =
  Response<CustomConfigRepresentation>;
