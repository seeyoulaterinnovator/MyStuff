import Resource from "../resource.js";
import {
  UserPostsResponseRepresentation,
  UserPostSystemRoleRepresentation,
} from "../../defs/custom/userRepresentation.js";
import {
  UserPostCreateRepresentation,
  UserPostRolesEditRepresentation,
  UserPostRolesResponseRepresentation,
  UserPostSystemRolesResponseRepresentation,
} from "../../defs/custom/userPostRepresentation.js";

export class UserPosts extends Resource {
  public findUserPosts = this.makeRequest<
    { realm: string; userId: string },
    UserPostsResponseRepresentation
  >({
    method: "GET",
    path: "/realms/{realm}/user-post/users/{userId}",
    urlParamKeys: ["realm", "userId"],
  });

  public findUserPostRoles = this.makeRequest<
    { realm: string },
    UserPostRolesResponseRepresentation
  >({
    method: "GET",
    path: "/realms/{realm}/user-post/roles",
    urlParamKeys: ["realm"],
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

  public createUserPost = this.makeUpdateRequest<
    { realm: string },
    UserPostCreateRepresentation,
    void
  >({
    method: "POST",
    path: "/realms/{realm}/user-post/create",
    urlParamKeys: ["realm"],
  });

  public deleteUserPost = this.makeUpdateRequest<
    { realm: string; postId: string },
    void,
    void
  >({
    method: "POST",
    path: "/realms/{realm}/user-post/delete/{postId}",
    urlParamKeys: ["realm", "postId"],
  });

  public findUserPostSystemRoles = this.makeRequest<
    { realm: string; realmId: string },
    UserPostSystemRolesResponseRepresentation
  >({
    method: "GET",
    path: "/realms/{realm}/user-post/system-roles",
    urlParamKeys: ["realm"],
    queryParamKeys: ["realmId"],
  });

  public createUserPostSystemRole = this.makeUpdateRequest<
    { realm: string },
    UserPostSystemRoleRepresentation,
    void
  >({
    method: "POST",
    path: "/realms/{realm}/user-post/add-system-role",
    urlParamKeys: ["realm"],
  });

  public deleteUserPostSystemRole = this.makeUpdateRequest<
    { realm: string },
    UserPostSystemRoleRepresentation,
    void
  >({
    method: "POST",
    path: "/realms/{realm}/user-post/remove-system-role",
    urlParamKeys: ["realm"],
  });
}
