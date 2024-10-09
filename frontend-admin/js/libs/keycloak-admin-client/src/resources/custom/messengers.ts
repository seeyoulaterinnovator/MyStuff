import Resource from "../resource.js";
import type { KeycloakAdminClient } from "../../client.js";
import { MessengersResponseRepresentation } from "../../defs/custom/messengerRepresentation.js";

export class CustomMessenger extends Resource<{ realm?: string }> {
  public getMessengers = this.makeRequest<{}, MessengersResponseRepresentation>(
    {
      method: "GET",
      path: "/messenger",
    },
  );

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
