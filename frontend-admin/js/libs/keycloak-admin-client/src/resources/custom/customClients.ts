import Resource from "../resource.js";
import type { KeycloakAdminClient } from "../../client.js";
import type ClientRepresentation from "../../defs/clientRepresentation.js";

// TODO mainRedirectUri
export class CustomClients extends Resource<{ realm?: string }> {
  public findOne = this.makeRequest<
    { id: string },
    ClientRepresentation | undefined
  >({
    method: "GET",
    path: "/{id}",
    urlParamKeys: ["id"],
    catchNotFound: true,
  });

  public update = this.makeUpdateRequest<
    { id: string },
    ClientRepresentation,
    void
  >({
    method: "PUT",
    path: "/{id}",
    urlParamKeys: ["id"],
  });

  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/realms/{realm}/custom-client",
      getUrlParams: () => ({
        realm: client.realmName,
      }),
      getBaseUrl: () => client.baseUrl,
    });
  }
}
