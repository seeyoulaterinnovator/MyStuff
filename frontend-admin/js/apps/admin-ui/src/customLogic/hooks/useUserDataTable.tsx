import type ComponentRepresentation from "@keycloak/keycloak-admin-client/lib/defs/componentRepresentation";
import type { CustomUserQuery } from "@keycloak/keycloak-admin-client/lib/resources/custom/users";
import type UserRepresentation from "@keycloak/keycloak-admin-client/lib/defs/userRepresentation";
import type {
  UserInfoRepresentation,
  UserPostRepresentation,
} from "@keycloak/keycloak-admin-client/lib/defs/custom/userRepresentation";
import { NetworkError } from "@keycloak/keycloak-admin-client/lib";
import { AlertVariant, Text, Tooltip } from "@patternfly/react-core";
import {
  CSSProperties,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react";
import { Link, useSearchParams } from "react-router-dom";
import { CustomUserToolbarAction } from "../constants/user";
import { useAdminClient } from "../../admin-client";
import { useAlerts } from "../../components/alert/Alerts";
import { useRealm } from "../../context/realm-context/RealmContext";
import { useRealms } from "../../context/RealmsContext";
import { toUser } from "../../user/routes/User";
import { type Field } from "../../components/table-toolbar/KeycloakDataTable";
import { useTranslation } from "react-i18next";
import { saveAs } from "file-saver";
import { useConfirmDialog } from "../../components/confirm-dialog/ConfirmDialog";
import { isExistGuard } from "../helpers/guards";
import type { CustomUsersAction } from "../types/users";
import { QueryParam } from "../constants/queryParams";
import { useCustomConfig } from "../context/CustomConfigContext";
import type { SortingOptions } from "../types/sorting";
import { useWhoAmI } from "../../context/whoami/WhoAmI";
import { addBomAndConvertToBlob } from "../helpers/transforms";
import { ExclamationCircleIcon } from "@patternfly/react-icons";

const getInvalidUsers = (
  users?: Array<UserRepresentation | UserInfoRepresentation>,
) => {
  const blockedUsers: (UserRepresentation | UserInfoRepresentation)[] = [];
  const blockedUsernames: string[] = [];
  const unverifiedUsers: (UserRepresentation | UserInfoRepresentation)[] = [];
  const unverifiedUsernames: string[] = [];

  users?.forEach((userItem) => {
    const { enabled, emailVerified, username } = userItem;

    if (!enabled) {
      blockedUsers.push(userItem);
      if (username) blockedUsernames.push(username);
    }

    if (!emailVerified) {
      unverifiedUsers.push(userItem);
      if (username) unverifiedUsernames.push(username);
    }
  });

  return {
    blockedUsers,
    blockedUsernames: blockedUsernames.join(", "),
    unverifiedUsers,
    unverifiedUsernames: unverifiedUsernames.join(", "),
  };
};

const ellipsisCellStyle: CSSProperties = {
  overflow: "hidden",
  textOverflow: "ellipsis",
  maxWidth: 0,
  minWidth: "100%",
  whiteSpace: "nowrap",
};

interface UserInfoWithFlatUserPosts
  extends UserInfoRepresentation,
    Partial<Omit<UserPostRepresentation, "id">> {
  userPostId?: UserPostRepresentation["id"];
}

export interface UseUserDataTableProps {
  selectedRows: Array<UserRepresentation | UserInfoRepresentation>;
  refresh: () => void;
  userStorage?: ComponentRepresentation[];
  listUsers?: boolean;
}

export const useUserDataTable = ({
  selectedRows,
  refresh,
  userStorage,
}: UseUserDataTableProps) => {
  const { adminClient } = useAdminClient();
  const { addAlert, addError } = useAlerts();
  const { t } = useTranslation();
  const { realms, setSearchRealm, accessibleRealms } = useRealms();
  const { realm: realmName } = useRealm();
  const [params] = useSearchParams();
  const paramSearchRealm = params.get(QueryParam.SEARCH_REALM);
  const rawFilterSearchRealm = paramSearchRealm || realmName;
  const [customFilters, setCustomFilters] = useState<CustomUserQuery>({
    searchRealm: accessibleRealms.some(
      (item) => item.name === rawFilterSearchRealm,
    )
      ? rawFilterSearchRealm
      : accessibleRealms[0]?.name,
  });

  const { searchRealm: filterSearchRealm } = customFilters;

  const { isMeInMaster } = useWhoAmI();
  const { isCustomTheme } = useCustomConfig();

  //should *only* list users when no user federation is configured
  const listUsers = !(userStorage && userStorage.length > 0);

  const selectedIds = useMemo(() => {
    return selectedRows.map((item) => item.id).filter(isExistGuard);
  }, [selectedRows]);

  const userColumns = useMemo<Field<UserInfoWithFlatUserPosts>[]>(() => {
    return [
      {
        name: "id",
        displayKey: "id",
        cellProps: {
          style: ellipsisCellStyle,
        },
        cellRenderer: (row) => {
          const href = toUser({
            id: row.id,
            realm: isMeInMaster
              ? customFilters.searchRealm || realmName
              : realmName,
            tab: "settings",
          }).pathname;

          if (!href) {
            return <Text>{row.id}</Text>;
          }

          return (
            <span>
              {!row.enabled && (
                <Tooltip content={t("notEnabled")}>
                  <ExclamationCircleIcon className="keycloak__user-section__email-verified" />
                </Tooltip>
              )}{" "}
              <Link to={href}>{row.id}</Link>
            </span>
          );
        },
      },
      {
        name: "email",
        displayKey: "email",
        isSortable: true,
        cellProps: {
          style: { ...ellipsisCellStyle, width: "20%" },
        },
        cellRenderer: (row) => {
          return (
            <span>
              {!row.emailVerified && (
                <Tooltip content={t("notVerified")}>
                  <ExclamationCircleIcon className="keycloak__user-section__email-verified" />
                </Tooltip>
              )}{" "}
              {row.email}
            </span>
          );
        },
      },
      // {
      //   name: "firstName",
      //   displayKey: "firstName",
      //   isSortable: true,
      //   cellProps: {
      //     style: ellipsisCellStyle,
      //   },
      // },
      {
        name: "phone",
        displayKey: "phone",
        cellProps: {
          style: { ...ellipsisCellStyle, width: "15%" },
        },
      },

      {
        name: "organization",
        displayKey: "organization",
        cellProps: {
          style: ellipsisCellStyle,
        },
      },
      {
        name: "tomsId",
        displayKey: "tomsId",
        cellProps: {
          style: { ...ellipsisCellStyle, width: "20%" },
        },
      },
      {
        name: "userRole",
        displayKey: "roles",
        cellProps: {
          style: { ...ellipsisCellStyle, width: "5%" },
        },
        cellRenderer: (row) => row.userRole?.name || "",
      },
      {
        name: "systemRoles",
        displayKey: "systems",
        cellProps: {
          style: ellipsisCellStyle,
        },
        cellRenderer: (row) => {
          return (row.systemRoles || [])
            .map((systemRole) => systemRole.externalSystem.name)
            .join(", ");
        },
      },
    ];
  }, [customFilters.searchRealm, isMeInMaster, realmName]);

  const getUsers = useCallback(
    async (query: CustomUserQuery) => {
      const response = await adminClient.customUsers.findUsers({
        ...{
          ...customFilters,
          ...query,
        },
      });
      const { "users-info": userInfo } = response.results;

      return userInfo;
    },
    [adminClient, customFilters],
  );

  const searchUserWithCustomFilters = (newCustomFilters: CustomUserQuery) => {
    setCustomFilters(newCustomFilters);
    refresh();

    const url = new URL(window.location.href);

    if (newCustomFilters.searchRealm) {
      url.searchParams.set(
        QueryParam.SEARCH_REALM,
        newCustomFilters.searchRealm,
      );
      history.pushState({}, "", url);
    } else {
      url.searchParams.delete(QueryParam.SEARCH_REALM);
    }
  };

  useEffect(() => {
    const url = new URL(window.location.href);
    const querySearchRealm = url.searchParams.get(QueryParam.SEARCH_REALM);
    setCustomFilters((prevCustomFilters) => ({
      ...prevCustomFilters,
      searchRealm: querySearchRealm || prevCustomFilters.searchRealm,
    }));

    return () => {
      const url = new URL(window.location.href);
      url.searchParams.delete(QueryParam.SEARCH_REALM);
      history.pushState({}, "", url);
    };
  }, []);

  useEffect(() => {
    setSearchRealm(filterSearchRealm);
  }, [filterSearchRealm]);

  useEffect(() => {
    return () => {
      setSearchRealm("");
    };
  }, []);

  const checkUsersForActivity = useCallback(() => {
    const {
      blockedUsers,
      blockedUsernames,
      unverifiedUsernames,
      unverifiedUsers,
    } = getInvalidUsers(selectedRows);

    if (blockedUsers?.length) {
      addError(
        t(
          blockedUsers.length > 1
            ? "blockedUsersSelected"
            : "blockedUserSelected",
          { username: blockedUsernames },
        ),
        "error",
      );
    }

    if (unverifiedUsers?.length) {
      addError(
        t(
          unverifiedUsers.length > 1
            ? "unverifiedUsersSelected"
            : "unverifiedUserSelected",
          { username: unverifiedUsernames },
        ),
        "error",
      );
    }

    if (blockedUsers?.length || unverifiedUsers?.length) {
      return true;
    }

    return false;
  }, [selectedRows]);

  const checkIsUsersNotSelected = useCallback(() => {
    if (!selectedRows.length) {
      addError(t("noUsersSelected"), "error");

      return true;
    }

    return false;
  }, [selectedRows]);

  const handleCustomAction = async (action: CustomUsersAction) => {
    switch (action.type) {
      case CustomUserToolbarAction.SEND_LOGIN: {
        try {
          if (checkIsUsersNotSelected() || checkUsersForActivity()) {
            return;
          }

          await adminClient.customUsers.sendLogin(
            { realm: filterSearchRealm },
            selectedIds,
          );

          addAlert(t("userLoginSentSuccess"), AlertVariant.success);
        } catch (error) {
          addError(t("userLoginSentError"), error);
        }

        break;
      }

      case CustomUserToolbarAction.SEND_LOGIN_AND_RESET_PASSWORD: {
        try {
          if (checkIsUsersNotSelected() || checkUsersForActivity()) {
            return;
          }

          await adminClient.customUsers.sendLoginAndResetPassword(
            { realm: filterSearchRealm },
            selectedIds,
          );

          addAlert(
            t("userLoginSentAndPasswordResetSuccess"),
            AlertVariant.success,
          );
        } catch (error) {
          addError(t("userLoginSentAndPasswordResetError"), error);
        }

        break;
      }

      case CustomUserToolbarAction.DOWNLOAD_TEMPLATE_CSV: {
        try {
          const downloadedFile =
            await adminClient.customUsers.downloadCSVTemplate({
              realm: filterSearchRealm,
            });

          saveAs(addBomAndConvertToBlob(downloadedFile), `user_template.csv`);
        } catch (error) {
          addError(t("userCSVTemplateDownloadError"), error);
        }

        break;
      }

      case CustomUserToolbarAction.DOWNLOAD_TEMPLATE_EXCEL: {
        try {
          const downloadedFile =
            await adminClient.customUsers.downloadExcelTemplate({
              realm: filterSearchRealm,
            });

          saveAs(
            new Blob([downloadedFile], {
              type: "application/octet-stream",
            }),
            `user_template.xlsx`,
          );
        } catch (error) {
          addError(t("userExcelTemplateDownloadError"), error);
        }

        break;
      }

      case CustomUserToolbarAction.IMPORT_FILE: {
        try {
          const { payload } = action;
          const formData = new FormData();

          if (payload) {
            formData.append("file", payload);

            await adminClient.customUsers.importFile(payload.name)(
              { realm: filterSearchRealm },
              formData,
            );

            toggleUploadUserInfo();
          }
        } catch (error: unknown) {
          if (error instanceof NetworkError) {
            switch (error.response.status) {
              case 400:
                addError(error.message, error);
                break;

              case 502:
                addAlert(t("tooManyUsersToImport"), AlertVariant.info);
                break;

              default:
                addError(error.response.statusText, error);
            }
          }
        }

        break;
      }

      case CustomUserToolbarAction.EXPORT_CSV:
      case CustomUserToolbarAction.EXPORT_EXCEL: {
        const isExcel = action.type === CustomUserToolbarAction.EXPORT_EXCEL;
        const downloadedExtension = isExcel ? "xlsx" : "csv";

        try {
          const downloadedFile = await adminClient.customUsers.downloadUsers(
            { realm: filterSearchRealm },
            {
              type: downloadedExtension,
              userIds: selectedIds,
              userParameters: [
                "USER_ID",
                "FIRST_NAME",
                "EMAIL",
                "PHONE",
                "TOMS_ID",
                "DMP_ID",
                "ROLE",
                "SYSTEM",
                "ENABLED",
              ],
            },
          );

          saveAs(
            isExcel
              ? new Blob([downloadedFile], {
                  type: "application/octet-stream",
                })
              : addBomAndConvertToBlob(downloadedFile),
            `user_info.${downloadedExtension}`,
          );
        } catch (error) {
          addError(t("usersExportedError"), error);
        }

        break;
      }

      case CustomUserToolbarAction.RESET_PASSWORD: {
        try {
          if (checkIsUsersNotSelected()) {
            return;
          }

          await adminClient.customUsers.resetPassword(
            { realm: realmName },
            selectedIds,
          );
          addAlert(t("userPasswordResetSuccess"), AlertVariant.success);
        } catch (error) {
          addError(t("userPasswordResetError"), error);
        }

        break;
      }

      case CustomUserToolbarAction.BLOCK_USERS: {
        try {
          if (checkIsUsersNotSelected()) {
            return;
          }

          await adminClient.customUsers.block(
            { realm: realmName },
            selectedIds,
          );
          addAlert(t("userBlockedSuccess"), AlertVariant.success);
          refresh();
        } catch (error) {
          addError(t("userBlockedError"), error);
        }

        break;
      }

      case CustomUserToolbarAction.UNLOCK_USERS: {
        try {
          if (checkIsUsersNotSelected()) {
            return;
          }

          await adminClient.customUsers.unlock(
            { realm: realmName },
            selectedIds,
          );
          addAlert(t("userUnlockedSuccess"), AlertVariant.success);
          refresh();
        } catch (error) {
          addError(t("userUnlockedError"), error);
        }

        break;
      }

      default:
        break;
    }
  };

  const [toggleUploadUserInfo, UploadUserInfo] = useConfirmDialog({
    titleKey: "information",
    messageKey: t("importUsersMessage", { count: selectedRows.length }),
    continueButtonLabel: "ok",
    noCancelButton: true,
    onConfirm: () => {
      return;
    },
  });

  const [sortingOptions, setSortingOptions] = useState<SortingOptions>();

  const customLoader = async (
    first?: number,
    max?: number,
    search?: string,
    newSortingOptions?: SortingOptions,
  ) => {
    if (!listUsers) {
      return [];
    }

    try {
      setSortingOptions(newSortingOptions);

      const users = await getUsers({
        first,
        max,
        sortAsc: newSortingOptions?.order === "asc",
        sortField: newSortingOptions?.orderBy,
      });
      const usersNew: UserInfoWithFlatUserPosts[] = [];

      users.forEach((user) => {
        if (!user.userPosts?.length) {
          usersNew.push(user);
        }

        user.userPosts?.forEach((userPost) => {
          const { id, ...userPostRest } = userPost;
          usersNew.push({ ...userPostRest, ...user, userPostId: id });
        });
      });

      return usersNew;
    } catch (error) {
      if (userStorage?.length) {
        addError("noUsersFoundErrorStorage", error);
      } else {
        addError("noUsersFoundError", error);
      }
      return [];
    }
  };

  return {
    sortingOptions,
    customFilters,
    isCustomTheme,
    realms,
    userColumns,
    customLoader,
    getUsers,
    handleCustomAction,
    searchUserWithCustomFilters,
    setCustomFilters,
    toggleUploadUserInfo,
    UploadUserInfo,
    setSortingOptions,
  };
};
