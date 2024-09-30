import { useWhoAmI } from "../context/whoami/WhoAmI";
import { useCustomConfig } from "../customLogic/context/CustomConfigContext";
import { useMatch, useNavigate } from "react-router-dom";
import { UserRoute } from "../user/routes/User";
import { toUsers, UsersRoute } from "../user/routes/Users";
import { RootRoute } from "../routes";
import { useEffect } from "react";
import { useRealm } from "../context/realm-context/RealmContext";
import { useErrorBoundary } from "../context/ErrorBoundary";
import { useTranslation } from "react-i18next";
import { AddUserRoute } from "../user/routes/AddUser";

export const CustomAuthWall = ({ children }: any) => {
  const { t } = useTranslation();
  const { showBoundary } = useErrorBoundary();
  const navigate = useNavigate();
  const { isCustomTheme } = useCustomConfig();
  const { realm } = useRealm();
  const { isMeInManager } = useWhoAmI();
  const isRootPage = useMatch(RootRoute.path || "");
  const isUsersPage = useMatch(UsersRoute.path);
  const isUserPage = useMatch(UserRoute.path);
  const isAddUserPage = useMatch(AddUserRoute.path);
  const isManagerPages =
    isRootPage || isUsersPage || isUserPage || isAddUserPage;
  const isForbidden = isCustomTheme && isMeInManager && !isManagerPages;

  useEffect(() => {
    if (isForbidden) {
      navigate(toUsers({ realm }));
    }
  }, []);

  if (isForbidden) {
    showBoundary(new Error(`${t("forbidden")}. ${t("noAccessResource")}.`));
  }

  return children;
};
