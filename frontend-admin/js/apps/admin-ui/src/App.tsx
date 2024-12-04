import KeycloakAdminClient from "@keycloak/keycloak-admin-client";
import {
  mainPageContentId,
  useEnvironment,
} from "@keycloak/keycloak-ui-shared";
import { Page } from "@patternfly/react-core";
import { PropsWithChildren, Suspense, useEffect, useState } from "react";
import { Outlet } from "react-router-dom";

import { Header } from "./PageHeader";
import { PageNav } from "./PageNav";
import { AdminClientContext, initAdminClient } from "./admin-client";
import { AlertProvider } from "./components/alert/Alerts";
import { PageBreadCrumbs } from "./components/bread-crumb/PageBreadCrumbs";
import { ErrorRenderer } from "./components/error/ErrorRenderer";
import { KeycloakSpinner } from "./components/keycloak-spinner/KeycloakSpinner";
import {
  ErrorBoundaryFallback,
  ErrorBoundaryProvider,
} from "./context/ErrorBoundary";
import { RealmsProvider, useRealms } from "./context/RealmsContext";
import { RecentRealmsProvider, useRecentRealms } from "./context/RecentRealms";
import { AccessContextProvider, useAccess } from "./context/access/Access";
import {
  RealmContextProvider,
  useRealm,
} from "./context/realm-context/RealmContext";
import {
  ServerInfoProvider,
  useServerInfo,
} from "./context/server-info/ServerInfoProvider";
import { useWhoAmI, WhoAmIContextProvider } from "./context/whoami/WhoAmI";
import type { Environment } from "./environment";
import { SubGroups } from "./groups/SubGroupsContext";
import { AuthWall } from "./root/AuthWall";
import {
  CustomConfigContextProvider,
  useCustomConfig,
} from "./customLogic/context/CustomConfigContext";
import { CustomAuthWall } from "./root/CustomAuthWall";
import { useClipboard } from "./customLogic/hooks/useClipboard";
import { HashRestorer } from "./root/HashRestorer";

const AppContextsReadyWall = ({ children }: PropsWithChildren) => {
  const serverInfo = useServerInfo();
  const { realmRepresentation, searchRealmRepresentation } = useRealm();
  const { whoAmI } = useWhoAmI();
  const { realms } = useRealms();
  const recentRealms = useRecentRealms();
  const access = useAccess();
  const config = useCustomConfig();

  if (
    !(
      serverInfo &&
      realmRepresentation &&
      searchRealmRepresentation &&
      whoAmI &&
      realms &&
      recentRealms &&
      access &&
      config
    )
  ) {
    return <KeycloakSpinner />;
  }

  return children;
};

const AppContexts = ({ children }: PropsWithChildren) => {
  return (
    <ErrorBoundaryProvider>
      <ErrorBoundaryFallback
        domain="context"
        fallback={(fallbackProps) => (
          <ErrorRenderer {...fallbackProps} withSignOut />
        )}
      >
        <ServerInfoProvider>
          <RealmContextProvider>
            <WhoAmIContextProvider>
              <RealmsProvider>
                <RecentRealmsProvider>
                  <AccessContextProvider>
                    <AlertProvider>
                      <CustomConfigContextProvider>
                        <SubGroups>{children}</SubGroups>
                      </CustomConfigContextProvider>
                    </AlertProvider>
                  </AccessContextProvider>
                </RecentRealmsProvider>
              </RealmsProvider>
            </WhoAmIContextProvider>
          </RealmContextProvider>
        </ServerInfoProvider>
      </ErrorBoundaryFallback>
    </ErrorBoundaryProvider>
  );
};

export const App = () => {
  useClipboard();
  const { keycloak, environment } = useEnvironment<Environment>();
  const [adminClient, setAdminClient] = useState<KeycloakAdminClient>();

  useEffect(() => {
    const init = async () => {
      const client = await initAdminClient(keycloak, environment);
      setAdminClient(client);
    };
    init().catch(console.error);
  }, []);

  if (!adminClient) return <KeycloakSpinner />;
  return (
    <AdminClientContext.Provider value={{ keycloak, adminClient }}>
      <AppContexts>
        <Page
          header={<Header />}
          isManagedSidebar
          sidebar={<PageNav />}
          breadcrumb={<PageBreadCrumbs />}
          mainContainerId={mainPageContentId}
        >
          <ErrorBoundaryFallback fallback={ErrorRenderer}>
            <Suspense fallback={<KeycloakSpinner />}>
              <HashRestorer>
                <AuthWall>
                  <CustomAuthWall>
                    <AppContextsReadyWall>
                      <Outlet />
                    </AppContextsReadyWall>
                  </CustomAuthWall>
                </AuthWall>
              </HashRestorer>
            </Suspense>
          </ErrorBoundaryFallback>
        </Page>
      </AppContexts>
    </AdminClientContext.Provider>
  );
};
