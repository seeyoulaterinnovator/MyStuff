import TimeUnit from "./timeUnits.js";

export type CustomSettingType = "REALM" | "FRONT" | "APP" | "EMAIL" | "MESSAGE";

export default interface CustomSettingRepresentation {
  name: string;
  id: string;
  extId: string;
  value: string;
  desc: string;
  realmId: string;
  unit: TimeUnit;
  type: CustomSettingType;
}
