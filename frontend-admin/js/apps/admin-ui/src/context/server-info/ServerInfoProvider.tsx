import type { ServerInfoRepresentation } from "@keycloak/keycloak-admin-client/lib/defs/serverInfoRepesentation";
import {
  createNamedContext,
  useRequiredContext,
} from "@keycloak/keycloak-ui-shared";
import { PropsWithChildren, useState } from "react";
import { useAdminClient } from "../../admin-client";
import { KeycloakSpinner } from "../../components/keycloak-spinner/KeycloakSpinner";
import { sortProviders } from "../../util";
import { useFetch } from "../../utils/useFetch";
import { NetworkError } from "@keycloak/keycloak-admin-client/lib";
import { useErrorBoundary } from "../ErrorBoundary";
import { useTranslation } from "react-i18next";

export const ServerInfoContext = createNamedContext<
  ServerInfoRepresentation | undefined
>("ServerInfoContext", undefined);

export const useServerInfo = () => useRequiredContext(ServerInfoContext);

export const useLoginProviders = () =>
  sortProviders(useServerInfo().providers!["login-protocol"].providers);

export const ServerInfoProvider = ({ children }: PropsWithChildren) => {
  const { adminClient } = useAdminClient();
  const [serverInfo, setServerInfo] = useState<ServerInfoRepresentation>();
  const { showBoundary } = useErrorBoundary();
  const { t } = useTranslation();

  useFetch(
    async () => {
      try {
        return await adminClient.serverInfo.find();
      } catch (error: any) {
        if (error instanceof NetworkError) {
          switch (error.response.status) {
            case 403:
              showBoundary(
                new Error(`${t("forbidden")}. ${t("noAccessResource")}.`),
                "context",
              );
              break;

            default:
              showBoundary(error, "context");
              break;
          }
        } else {
          showBoundary(error, "context");
        }
      }
    },
    setServerInfo,
    [],
  );

  if (!serverInfo) {
    return <KeycloakSpinner />;
  }

  return (
    <ServerInfoContext.Provider value={serverInfo}>
      {children}
    </ServerInfoContext.Provider>
  );
};
