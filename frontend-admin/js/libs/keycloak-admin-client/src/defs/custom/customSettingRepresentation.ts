import TimeUnit from "./timeUnits.js";
import type { Response } from "../jsonResponse.js";

export type CustomSettingType = "REALM" | "FRONT" | "APP" | "EMAIL" | "MESSAGE";

export type CustomSettingRepresentation = {
  name: string;
  id: string;
  extId: string;
  value: string;
  desc: string;
  realmId: string;
  unit: TimeUnit;
  type: CustomSettingType;
};

export type CustomSettingsResultRepresentation = {
  settings: CustomSettingRepresentation[];
};

export interface CustomSettingsResponseRepresentation
  extends Response<CustomSettingsResultRepresentation, "httpStatus"> {}

export type CustomSettingResultRepresentation = {
  setting: CustomSettingRepresentation;
};

export interface CustomSettingResponseRepresentation
  extends Response<CustomSettingResultRepresentation, "httpStatus"> {}
