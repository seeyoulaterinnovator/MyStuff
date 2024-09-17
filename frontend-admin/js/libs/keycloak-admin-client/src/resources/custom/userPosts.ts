import Resource from "../resource.js";
import {UserPostsResponseRepresentation} from "../../defs/custom/userRepresentation.js";

export class UserPosts extends Resource {
  public findUserPosts = this.makeRequest<
    { realm: string, userId: string },
    UserPostsResponseRepresentation
  >({
    method: "GET",
    path: "/realms/{realm}/user-post/users/{userId}",
    urlParamKeys: ["realm", "userId"]
  });
}
