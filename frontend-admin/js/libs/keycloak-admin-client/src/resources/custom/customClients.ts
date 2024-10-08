import Resource from "../resource.js";
import type { KeycloakAdminClient } from "../../client.js";
import { CustomClientRepresentation } from "../../defs/custom/customClientRepresentation.js";

export class CustomClients extends Resource<{ realm?: string }> {
  public findOne = this.makeRequest<
    { id: string },
    CustomClientRepresentation | undefined
  >({
    method: "GET",
    path: "/{id}/ext",
    urlParamKeys: ["id"],
    catchNotFound: true,
  });

  public update = this.makeUpdateRequest<
    { id: string },
    CustomClientRepresentation,
    void
  >({
    method: "PUT",
    path: "/{id}/ext",
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
