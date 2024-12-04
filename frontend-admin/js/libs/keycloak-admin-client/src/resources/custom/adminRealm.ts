import type { KeycloakAdminClient } from "../../client.js";
import Resource from "../resource.js";

export class CustomAdminRealm extends Resource<{ realm?: string }> {
  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/admin/realms/{realm}",
      getUrlParams: () => ({
        realm: client.realmName,
      }),
      getBaseUrl: () => client.baseUrl,
    });
  }
}
