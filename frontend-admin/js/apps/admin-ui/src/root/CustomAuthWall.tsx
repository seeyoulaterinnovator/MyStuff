import { useWhoAmI } from "../context/whoami/WhoAmI";
import { useCustomConfig } from "../customLogic/context/CustomConfigContext";
import { useMatch, useNavigate } from "react-router-dom";
import { UserRoute } from "../user/routes/User";
import { toUsers, UsersRoute } from "../user/routes/Users";
import { RootRoute } from "../routes";
import { useEffect } from "react";
import { useRealm } from "../context/realm-context/RealmContext";

export const CustomAuthWall = ({ children }: any) => {
  const navigate = useNavigate();
  const { isCustomTheme } = useCustomConfig();
  const { realm } = useRealm();
  const { isMeInManager } = useWhoAmI();
  const isRootPage = useMatch(RootRoute.path || "");
  const isUsersPage = useMatch(UsersRoute.path);
  const isUserPage = useMatch(UserRoute.path);
  const isManagerPages = isRootPage || isUsersPage || isUserPage;
  const isForbidden = isCustomTheme && isMeInManager && !isManagerPages;

  useEffect(() => {
    if (isForbidden) {
      navigate(toUsers({ realm }));
    }
  }, []);

  if (isForbidden) return null;

  return children;
};
