import Resource from "../resource.js";
import {UserPostsResponseRepresentation} from "../../defs/custom/userRepresentation.js";
import {
  UserPostRolesEditRepresentation,
  UserPostRolesResponseRepresentation
} from "../../defs/custom/userPostRoleRepresentation.js";

export class UserPosts extends Resource {
  public findUserPosts = this.makeRequest<
    { realm: string, userId: string },
    UserPostsResponseRepresentation
  >({
    method: "GET",
    path: "/realms/{realm}/user-post/users/{userId}",
    urlParamKeys: ["realm", "userId"]
  });

  public findUserPostRoles = this.makeRequest<
    { realm: string },
    UserPostRolesResponseRepresentation
  >({
    method: "GET",
    path: "/realms/{realm}/user-post/roles",
    urlParamKeys: ["realm"]
  });

  public updateUserPostRole = this.makeUpdateRequest<
    { realm: string },
    UserPostRolesEditRepresentation,
    void
  >({
    method: "POST",
    path: "/realms/{realm}/user-post/edit",
    urlParamKeys: ["realm"],
  });
}
