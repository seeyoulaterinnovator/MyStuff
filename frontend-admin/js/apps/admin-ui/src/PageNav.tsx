import {
  Divider,
  Flex,
  FlexItem,
  Label,
  Nav,
  NavGroup,
  NavItem,
  NavList,
  PageSidebar,
  PageSidebarBody,
} from "@patternfly/react-core";
import { FormEvent } from "react";
import { useTranslation } from "react-i18next";
import { NavLink, useMatch, useNavigate } from "react-router-dom";
import { RealmSelector } from "./components/realm-selector/RealmSelector";
import { useAccess } from "./context/access/Access";
import { useRealm } from "./context/realm-context/RealmContext";
import { useServerInfo } from "./context/server-info/ServerInfoProvider";
import { toPage } from "./page/routes";
import { AddRealmRoute } from "./realm/routes/AddRealm";
import { DashboardRoute } from "./dashboard/routes/Dashboard";
import { routes } from "./routes";
import useIsFeatureEnabled, { Feature } from "./utils/useIsFeatureEnabled";
import { useRealms } from "./context/RealmsContext";

import "./page-nav.css";
import { useCustomConfig } from "./customLogic/context/CustomConfigContext";
import { useWhoAmI } from "./context/whoami/WhoAmI";

type LeftNavProps = { title: string; path: string; id?: string, label?: string, realm?: string };

const LeftNav = ({ title, path, id, label, realm }: LeftNavProps) => {
  const { t } = useTranslation();
  const { hasAccess } = useAccess();
  const { realm: defaultRealm } = useRealm();
  const encodedRealm = encodeURIComponent(defaultRealm);
  const route = routes.find(
    (route) =>
      route.path.replace(/\/:.+?(\?|(?:(?!\/).)*|$)/g, "") === (id || path),
  );

  const accessAllowed =
    route &&
    (route.handle.access instanceof Array
      ? hasAccess(...route.handle.access)
      : hasAccess(route.handle.access));

  if (!accessAllowed) {
    return null;
  }

  return (
    <li>
      <NavLink
        id={"nav-item" + path.replace("/", "-")}
        to={`/${encodedRealm}${path}` + (realm ? `?searchRealm=${encodeURIComponent(realm)}` : "")}
        className={({ isActive }) =>
          `pf-v5-c-nav__link${isActive ? " pf-m-current" : ""}`
        }
      >
        <Flex>
          <FlexItem>
            {t(title)}
          </FlexItem>
          {label && (
            <FlexItem>
              <Label isCompact color="blue">{label}</Label>
            </FlexItem>
          )}
        </Flex>
      </NavLink>
    </li>
  );
};

export const PageNav = () => {
  const { t } = useTranslation();
  const { hasSomeAccess } = useAccess();
  const { componentTypes } = useServerInfo();
  const isFeatureEnabled = useIsFeatureEnabled();
  const pages =
    componentTypes?.["org.keycloak.services.ui.extend.UiPageProvider"];
  const navigate = useNavigate();
  const { realmRepresentation } = useRealm();
  const { realms, searchRealm: listSearchRealm } = useRealms();
  const { realm, searchRealm: cardSearchRealm } = useRealm();
  const { isMeInManager } = useWhoAmI();
  const { isCustomTheme } = useCustomConfig();

  type SelectedItem = {
    groupId: number | string;
    itemId: number | string;
    to: string;
    event: FormEvent<HTMLInputElement>;
  };

  const onSelect = (item: SelectedItem) => {
    navigate(item.to);
    item.event.preventDefault();
  };

  const showManage = hasSomeAccess(
    "view-realm",
    "query-groups",
    "query-users",
    "query-clients",
    "view-events",
  );

  const showConfigure = hasSomeAccess(
    "view-realm",
    "query-clients",
    "view-identity-providers",
  );

  const isOnAddRealm = !!useMatch(AddRealmRoute.path);
  const isRootPage = !!useMatch(DashboardRoute.path);
  const isCustomRootPage =
    (isCustomTheme && isRootPage && realms.length > 1) ||
    !isCustomTheme;

  const UsersLeftNav = () => {
    if(isCustomTheme) {
      const otherRealm = realm !== cardSearchRealm ? cardSearchRealm :
          listSearchRealm && realm !== listSearchRealm ? listSearchRealm : "";
      return <LeftNav title="users" path="/users" realm={otherRealm} label={otherRealm} />;
    } else {
      return <LeftNav title="users" path="/users" />;
    }
  }

  return (
    <PageSidebar className="keycloak__page_nav__nav">
      <PageSidebarBody>
        <Nav onSelect={(_event, item) => onSelect(item as SelectedItem)}>
          <NavList>
            <NavItem className="keycloak__page_nav__nav_item__realm-selector">
              <RealmSelector />
            </NavItem>
          </NavList>
          <Divider />
          {!isCustomRootPage && showManage && !isOnAddRealm && (
            <NavGroup aria-label={t("manage")} title={t("manage")}>
              {(isCustomTheme && isMeInManager) ? (
                  <UsersLeftNav/>
              ) : (
                <>
                  {isFeatureEnabled(Feature.Organizations) &&
                    realmRepresentation?.organizationsEnabled && (
                      <LeftNav title="organizations" path="/organizations" />
                    )}
                  <LeftNav title="clients" path="/clients" />
                  <LeftNav title="clientScopes" path="/client-scopes" />
                  <LeftNav title="realmRoles" path="/roles" />
                  <UsersLeftNav/>
                  <LeftNav title="importUsers" path="/import-users" />
                  <LeftNav title="groups" path="/groups" />
                  <LeftNav title="sessions" path="/sessions" />
                  <LeftNav title="events" path="/events" />
                </>
              )}
            </NavGroup>
          )}

          {!isCustomRootPage && showConfigure && !isOnAddRealm && !(isCustomTheme && isMeInManager) && (
            <NavGroup aria-label={t("configure")} title={t("configure")}>
              <LeftNav title="realmSettings" path="/realm-settings" />
              <LeftNav title="authentication" path="/authentication" />
              <LeftNav title="identityProviders" path="/identity-providers" />
              <LeftNav title="userFederation" path="/user-federation" />
              {isFeatureEnabled(Feature.DeclarativeUI) &&
                pages?.map((p) => (
                  <LeftNav
                    key={p.id}
                    title={p.id}
                    path={toPage({ providerId: p.id }).pathname!}
                    id="/page-section"
                  />
                ))}
              {isCustomTheme && (
                <LeftNav title="customSettings" path="/custom-settings" />
              )}
            </NavGroup>
          )}
        </Nav>
      </PageSidebarBody>
    </PageSidebar>
  );
};
