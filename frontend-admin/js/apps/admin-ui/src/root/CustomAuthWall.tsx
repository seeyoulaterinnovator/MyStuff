import { useWhoAmI } from "../context/whoami/WhoAmI";
import { useCustomConfig } from "../customLogic/context/CustomConfigContext";
import { useMatch, useNavigate } from "react-router-dom";
import { UserRoute } from "../user/routes/User";
import { toUsers, UsersRoute } from "../user/routes/Users";
import { RootRoute } from "../routes";
import { useRealm } from "../context/realm-context/RealmContext";
import { AddUserRoute } from "../user/routes/AddUser";

export const CustomAuthWall = ({ children }: any) => {
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
  if (isForbidden && !isUsersPage) {
    navigate(toUsers({ realm }));
  }
  return children;
};
