import {
  ActiveImportUsersResponseRepresentation,
  DownloadedUsersRepresentation,
  ImportUsersReportResponseRepresentation,
  RealmNameRepresentation,
  UserAccessibleRealmsRepresentation,
  UserFindResponseRepresentation,
  UsersResponseRepresentation,
} from "../../defs/custom/userRepresentation.js";
import type { KeycloakAdminClient } from "../../client.js";
import Resource from "../resource.js";
import { CustomAdminRealm } from "./adminRealm.js";
import RoleRepresentation, {RoleMappingPayload} from "../../defs/roleRepresentation.js";

export type CustomUserQuery = Partial<{
  searchRealm: string;
  search: string;
  searchUser: string;
  searchToms: string;
  searchPhone: string;
  first: number;
  max: number;
  sortAsc: string;
  sortField: string;
}>;

export type CustomImportUsersQuery = Partial<{
  first: number;
  max: number;
}>;

export class CustomUsers extends Resource<{ realm?: string }> {
  protected customAdminRealm: CustomAdminRealm;

  public delete;

  public findUsers = this.makeRequest<
    CustomUserQuery,
    UsersResponseRepresentation
  >({
    method: "GET",
    path: "/users-info/search",
    queryParamKeys: [
      "searchRealm",
      "search",
      "searchUser",
      "searchToms",
      "searchPhone",
      "first",
      "max",
      "sortAsc",
      "sortField",
    ],
  });

  public getAccessibleRealms = this.makeRequest<
    unknown,
    UserAccessibleRealmsRepresentation
  >({
    method: "GET",
    path: "/users-info/accessible-realms",
  });

  public sendLogin = this.makeUpdateRequest<
    unknown,
    string[], // userIds
    void
  >({
    method: "POST",
    path: "/users-toms/send/login",
  });

  public sendLoginAndResetPassword = this.makeUpdateRequest<
    unknown,
    string[], // userIds
    void
  >({
    method: "POST",
    path: "/users-toms/credential/reset-with-send-login",
  });

  public downloadCSVTemplate = this.makeRequest<unknown, ArrayBuffer>({
    method: "POST",
    path: "/users-toms/downloadImportUsersTemplate/csv",
    headers: {
      accept: "application/octet-stream",
    },
  });

  public downloadExcelTemplate = this.makeRequest<unknown, ArrayBuffer>({
    method: "POST",
    path: "/users-toms/downloadImportUsersTemplate/xlsx",
    headers: {
      accept: "application/octet-stream",
    },
  });

  public importFile = (filename?: string) =>
    this.makeUpdateRequest<unknown, FormData>({
      method: "POST",
      path: "/users-toms/uploadUsers",
      headers: {
        "Content-Disposition": `form-data; name="file"; filename=${filename}`,
      },
    });

  public downloadUsers = this.makeUpdateRequest<
    unknown,
    DownloadedUsersRepresentation,
    ArrayBuffer
  >({
    method: "POST",
    path: "/users-toms/downloadUsers",
  });

  public getImportsReport = this.makeRequest<
    CustomImportUsersQuery,
    ImportUsersReportResponseRepresentation
  >({
    method: "GET",
    path: "/users-toms/importUsersReports",
    queryParamKeys: ["first", "max"],
  });

  public downloadImportUsersReport = this.makeRequest<
    { id: string },
    ArrayBuffer
  >({
    method: "POST",
    path: "/users-toms/downloadImportUsersReport/{id}",
    urlParamKeys: ["id"],
  });

  public activeImportUsersReport = this.makeRequest<
    { id: string },
    ActiveImportUsersResponseRepresentation
  >({
    method: "POST",
    path: "/users-toms/activateImportUsersReport/{id}",
    urlParamKeys: ["id"],
  });

  public resetPassword = this.makeUpdateRequest<
    unknown,
    string[], // userIds
    void
  >({
    method: "POST",
    path: "/manage/credential/reset",
  });

  public block = this.makeUpdateRequest<
    unknown,
    string[], // userIds
    void
  >({
    method: "POST",
    path: "/manage/block",
  });

  public unlock = this.makeUpdateRequest<
    unknown,
    string[], // userIds
    void
  >({
    method: "POST",
    path: "/manage/unlock",
  });

  public impersonation = this.makeUpdateRequest<
    { id: string },
    { user: string; realm: string },
    Record<string, any>
  >({
    method: "POST",
    path: "/users-toms/impersonation/{id}",
    urlParamKeys: ["id"],
  });

  public findUserByAttribute = this.makeRequest<
    { realm: string; phone: string; excludedUserId: string; realmId: string },
    UserFindResponseRepresentation
  >({
    method: "GET",
    path: "/users-info/attribute",
    urlParamKeys: ["realm"],
    queryParamKeys: ["phone", "excludedUserId", "realmId"],
  });

  public findRealmNameByUserId = this.makeRequest<
    { userId: string },
    RealmNameRepresentation
  >({
    method: "GET",
    path: "/users-info/realm-name-by-user-id",
    queryParamKeys: [
      "userId",
    ],
  });

  // name fixed
  public listAvailableRealmRoleMappings = this.makeRequest<
    { id: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/users-toms/role-mappings/{id}/realm/available",
    urlParamKeys: ["id"],
  });

  // name fixed
  public listAvailableClientRoleMappings = this.makeRequest<
    { id: string; clientUniqueId: string },
    RoleRepresentation[]
  >({
    method: "GET",
    path: "/users-toms/role-mappings/{id}/clients/{clientUniqueId}/available",
    urlParamKeys: ["id", "clientUniqueId"],
  });

  public addRealmRoleMappings = this.makeRequest<
    { id: string; roles: RoleMappingPayload[] },
    void
  >({
    method: "POST",
    path: "/users-toms/role-mappings/{id}/realm",
    urlParamKeys: ["id"],
    payloadKey: "roles",
  });

  public addClientRoleMappings = this.makeRequest<
    {
      id: string;
      clientUniqueId: string;
      roles: RoleMappingPayload[];
    },
    void
  >({
    method: "POST",
    path: "/users-toms/role-mappings/{id}/clients/{clientUniqueId}",
    urlParamKeys: ["id", "clientUniqueId"],
    payloadKey: "roles",
  });

  // name fixed
  public delRealmRoleMappings = this.makeRequest<
    { id: string; roles: RoleMappingPayload[] },
    void
  >({
    method: "DELETE",
    path: "/users-toms/role-mappings/{id}/realm",
    urlParamKeys: ["id"],
    payloadKey: "roles",
  });

  // name fixed
  public delClientRoleMappings = this.makeRequest<
    { id: string; clientUniqueId: string; roles: RoleMappingPayload[] },
    void
  >({
    method: "DELETE",
    path: "/users-toms/role-mappings/{id}/clients/{clientUniqueId}",
    urlParamKeys: ["id", "clientUniqueId"],
    payloadKey: "roles",
  });

  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/realms/{realm}",
      getUrlParams: () => ({
        realm: client.realmName,
      }),
      getBaseUrl: () => client.baseUrl,
    });

    this.customAdminRealm = new CustomAdminRealm(client);

    this.delete = this.customAdminRealm.makeRequest<{ id: string }, void>({
      method: "DELETE",
      path: "/users/{id}",
      urlParamKeys: ["id"],
    });
  }
}
