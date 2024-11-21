import type { Response } from "../jsonResponse.js";

export type CustomConfigRepresentation = {
  adminTheme?: string;
  adminAllowedOrigins?: string[];
};

export type CustomConfigResponseRepresentation =
  Response<CustomConfigRepresentation>;
