import Resource from "./resource.js";
import CustomSettingsResultRepresentation from "../defs/customSettingsResultRepresentation.js";
import CustomSettingRepresentation from "../defs/customSettingRepresentation.js";
import CustomSettingResultRepresentation from "../defs/customSettingResultRepresentation.js";

export class CustomSettings extends Resource {
  public findSettings = this.makeRequest<
    { realm: string, type: string },
    CustomSettingsResultRepresentation
  >({
    method: "GET",
    path: "/realms/{realm}/settings/search",
    urlParamKeys: ["realm"],
    queryParamKeys: [
      "type",
    ],
  });

  public updateSetting = this.makeUpdateRequest<
    { realm: string, settingId: string },
    CustomSettingRepresentation,
    CustomSettingResultRepresentation
  >({
    method: "PUT",
    path: "/realms/{realm}/settings/{settingId}",
    urlParamKeys: ["realm", "settingId"],
  });
}
