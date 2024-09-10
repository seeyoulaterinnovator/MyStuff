import Resource from "./resource.js";
import CustomSettingsRepresentation from "../defs/customSettingsRepresentation.js";

export class CustomSettings extends Resource {
  public find = this.makeRequest<
    { realm: string, type: string },
    CustomSettingsRepresentation
  >({
    method: "GET",
    path: "/realms/{realm}/settings/search",
    urlParamKeys: ["realm"],
    queryParamKeys: [
      "type",
    ],
  });
}
