import { PropsWithChildren, useEffect, useMemo, useState } from "react";
import { useMatch, useNavigate } from "react-router-dom";
import {
  createNamedContext,
  useEnvironment,
  useRequiredContext,
} from "@keycloak/keycloak-ui-shared";
import { useAdminClient } from "../../admin-client";
import { DashboardRouteWithRealm } from "../../dashboard/routes/Dashboard";
import RealmRepresentation from "@keycloak/keycloak-admin-client/lib/defs/realmRepresentation";
import { useFetch } from "../../utils/useFetch";
import { UserParams, UserRoute } from "../../user/routes/User";
import { useParams } from "../../utils/useParams";

type RealmContextType = {
  realm: string;
  searchRealm: string;
  searchRealmUserId?: string;
  realmRepresentation?: RealmRepresentation;
  searchRealmRepresentation?: RealmRepresentation;
  refresh: () => void;
};

export const RealmContext = createNamedContext<RealmContextType | undefined>(
  "RealmContext",
  undefined,
);

export const RealmContextProvider = ({ children }: PropsWithChildren) => {
  const { adminClient } = useAdminClient();
  const { environment } = useEnvironment();
  const [key, setKey] = useState(0);
  const refresh = () => setKey(key + 1);
  const [realmRepresentation, setRealmRepresentation] =
    useState<RealmRepresentation>();
  const isOnUserPage = !!useMatch(UserRoute.path);
  const { id: userId } = useParams<UserParams>();
  const [searchRealm, setSearchRealm] = useState<string | null>(null);
  const [searchRealmRepresentation, setSearchRealmRepresentation] =
    useState<RealmRepresentation>();
  const navigate = useNavigate();

  const routeMatch = useMatch({
    path: DashboardRouteWithRealm.path,
    end: false,
  });

  const realmParam = routeMatch?.params.realm;
  const realm = useMemo(
    () => decodeURIComponent(realmParam ?? environment.realm),
    [realmParam, environment.realm],
  );

  // Configure admin client to use selected realm when it changes.
  useEffect(() => adminClient.setConfig({ realmName: realm }), [realm]);
  useFetch(
    () => adminClient.realms.findOne({ realm }),
    setRealmRepresentation,
    [realm, key],
  );

  useFetch(
    () => {
      if (isOnUserPage && userId) {
        return adminClient.customUsers.findRealmNameByUserId({
          userId,
        });
      } else {
        return Promise.resolve(null);
      }
    },
    (result) => {
      if (result) {
        setSearchRealm(result.realm);
      } else {
        setSearchRealm(null);
      }
    },
    [isOnUserPage, userId, realm],
  );
  useEffect(() => {
    if (!isOnUserPage) {
      setSearchRealm(null);
    }
  }, [isOnUserPage]);

  useEffect(() => {
    if (realm === "realms") {
      // fix для случая входа под админом после выхода из-под менеджера (слетает realm из-за разницы в base uri)
      navigate("/master/console");
      window.location.href = window.location.origin + window.location.pathname;
    }
  }, [realm]);

  useFetch(
    async () => {
      if (userId && searchRealm && realm !== searchRealm) {
        return adminClient.customUsers.findUserRealm({
          id: userId,
          realm: searchRealm,
        });
      } else {
        return Promise.resolve(undefined);
      }
    },
    setSearchRealmRepresentation,
    [realm, searchRealm, userId],
  );

  return (
    <RealmContext.Provider
      value={{
        realm,
        searchRealm:
          searchRealm && searchRealmRepresentation ? searchRealm : realm,
        realmRepresentation,
        searchRealmRepresentation:
          searchRealm && searchRealmRepresentation
            ? searchRealmRepresentation
            : realmRepresentation,
        searchRealmUserId:
          isOnUserPage && searchRealm && searchRealm !== realm
            ? userId
            : undefined,
        refresh,
      }}
    >
      {children}
    </RealmContext.Provider>
  );
};

export const useRealm = () => useRequiredContext(RealmContext);
