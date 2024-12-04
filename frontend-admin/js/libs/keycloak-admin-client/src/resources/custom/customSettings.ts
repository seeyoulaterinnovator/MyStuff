import Resource from "../resource.js";
import {
  CustomSettingRepresentation,
  CustomSettingResponseRepresentation,
  CustomSettingsResponseRepresentation,
} from "../../defs/custom/customSettingRepresentation.js";

export class CustomSettings extends Resource {
  public findSettings = this.makeRequest<
    { realm: string; type: string },
    CustomSettingsResponseRepresentation
  >({
    method: "GET",
    path: "/realms/{realm}/settings/search",
    urlParamKeys: ["realm"],
    queryParamKeys: ["type"],
  });

  public updateSetting = this.makeUpdateRequest<
    { realm: string; settingId: string },
    CustomSettingRepresentation,
    CustomSettingResponseRepresentation
  >({
    method: "PUT",
    path: "/realms/{realm}/settings/{settingId}",
    urlParamKeys: ["realm", "settingId"],
  });
}
