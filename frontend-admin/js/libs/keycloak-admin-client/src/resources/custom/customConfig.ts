import Resource from "../resource.js";
import type { KeycloakAdminClient } from "../../client.js";
import { CustomConfigResponseRepresentation } from "../../defs/custom/customConfigRepresentation.js";

export class CustomConfig extends Resource<{ realm?: string }> {
  public getCustomConfig = this.makeRequest<
    {},
    CustomConfigResponseRepresentation
  >({
    method: "GET",
    path: "/config-custom/admin",
  });

  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/realms/{realm}",
      getUrlParams: () => ({
        realm: client.realmName,
      }),
      getBaseUrl: () => client.baseUrl,
    });
  }
}
