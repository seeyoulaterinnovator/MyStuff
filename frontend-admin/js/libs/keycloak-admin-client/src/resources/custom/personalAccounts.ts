import Resource from "../resource.js";
import {PersonalAccountPostsResponseRepresentation} from "../../defs/custom/personalAccountRepresentation.js";

export class PersonalAccounts extends Resource {
  public findPersonalAccounts = this.makeRequest<
    { realm: string, postId: string },
    PersonalAccountPostsResponseRepresentation
  >({
    method: "GET",
    path: "/realms/{realm}/personal-account/{postId}",
    urlParamKeys: ["realm", "postId"]
  });
}
