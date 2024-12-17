import Resource from "../resource.js";
import type { KeycloakAdminClient } from "../../client.js";

export class CustomCache extends Resource<{ realm?: string }> {
  public clearCustomerCache = this.makeRequest<{}, void>({
    method: "POST",
    path: "/clear-customer-cache",
  });

  public clearUserPostCache = this.makeRequest<{}, void>({
    method: "POST",
    path: "/clear-user-post-cache",
  });

  constructor(client: KeycloakAdminClient) {
    super(client, {
      path: "/realms/{realm}/custom-cache",
      getUrlParams: () => ({
        realm: client.realmName,
      }),
      getBaseUrl: () => client.baseUrl,
    });
  }
}
