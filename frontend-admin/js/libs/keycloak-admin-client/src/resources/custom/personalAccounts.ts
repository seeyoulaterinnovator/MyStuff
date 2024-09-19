import Resource from "../resource.js";
import {
  PersonalAccountIdsRepresentation,
  PersonalAccountPostResponseRepresentation,
  PersonalAccountsResponseRepresentation,
  PersonalAccountValuesRepresentation,
} from "../../defs/custom/personalAccountRepresentation.js";

export class PersonalAccounts extends Resource {
  public findPersonalAccounts = this.makeRequest<
    { realm: string; postId: string },
    PersonalAccountPostResponseRepresentation
  >({
    method: "GET",
    path: "/realms/{realm}/personal-account/{postId}",
    urlParamKeys: ["realm", "postId"],
  });

  public createPersonalAccounts = this.makeUpdateRequest<
    { realm: string; postId: string },
    PersonalAccountValuesRepresentation,
    PersonalAccountsResponseRepresentation
  >({
    method: "PATCH",
    path: "/realms/{realm}/personal-account/{postId}/addV2",
    urlParamKeys: ["realm", "postId"],
  });

  public deletePersonalAccounts = this.makeUpdateRequest<
    { realm: string; postId: string },
    PersonalAccountIdsRepresentation,
    void
  >({
    method: "PATCH",
    path: "/realms/{realm}/personal-account/{postId}/sub",
    urlParamKeys: ["realm", "postId"],
  });
}
