import type ComponentRepresentation from "@keycloak/keycloak-admin-client/lib/defs/componentRepresentation";
import type { CustomUserQuery } from "@keycloak/keycloak-admin-client/lib/resources/custom/users";
import type UserRepresentation from "@keycloak/keycloak-admin-client/lib/defs/userRepresentation";
import type {
  UserInfoRepresentation,
  UserPostRepresentation,
} from "@keycloak/keycloak-admin-client/lib/defs/custom/userRepresentation";
import { NetworkError } from "@keycloak/keycloak-admin-client/lib";
import {
  AlertVariant,
  ButtonVariant,
  Text,
  Tooltip,
} from "@patternfly/react-core";
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
import {
  useConfirmDialog,
  type ConfirmDialogProps,
} from "../../components/confirm-dialog/ConfirmDialog";
import { isExistGuard } from "../helpers/guards";
import type { CustomUsersAction } from "../types/users";
import { QueryParam } from "../constants/queryParams";
import { useCustomConfig } from "../context/CustomConfigContext";
import type { SortingOptions } from "../types/sorting";
import { useWhoAmI } from "../../context/whoami/WhoAmI";
import { addBomAndConvertToBlob } from "../helpers/transforms";
import { ExclamationCircleIcon } from "@patternfly/react-icons";
import type PageInfoRepresentation from "js/libs/keycloak-admin-client/lib/defs/custom/pageInfoRepresentation";

const getInvalidUsers = (
  users?: Array<UserRepresentation | UserInfoRepresentation>,
) => {
  const blockedUsers: (UserRepresentation | UserInfoRepresentation)[] = [];
  const blockedUsernames: string[] = [];
  const unlockedUserIds: string[] = [];
  const unverifiedUsers: (UserRepresentation | UserInfoRepresentation)[] = [];
  const unverifiedUsernames: string[] = [];
  const verifiedUserIds: string[] = [];
  const unlockedAndVerifiedUserIds: string[] = [];

  const processUser = (
    condition: boolean | undefined,
    checkedUser: UserRepresentation | UserInfoRepresentation,
    users: (UserRepresentation | UserInfoRepresentation)[],
    usernames: string[],
    validIds: string[],
  ) => {
    const { username, id } = checkedUser;

    if (condition) {
      users.push(checkedUser);
      if (username) usernames.push(username);
    } else if (id) {
      validIds.push(id);
    }
  };

  users?.forEach((userItem) => {
    processUser(
      !userItem.enabled,
      userItem,
      blockedUsers,
      blockedUsernames,
      unlockedUserIds,
    );

    processUser(
      !userItem.emailVerified,
      userItem,
      unverifiedUsers,
      unverifiedUsernames,
      verifiedUserIds,
    );

    processUser(
      !userItem.emailVerified && !userItem.enabled,
      userItem,
      [],
      [],
      unlockedAndVerifiedUserIds,
    );
  });

  return {
    blockedUsers,
    blockedUsernames: blockedUsernames.join(", "),
    unlockedUserIds,
    unverifiedUsers,
    unverifiedUsernames: unverifiedUsernames.join(", "),
    verifiedUserIds,
    unlockedAndVerifiedUserIds,
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
  refresh: () => void;
  userStorage?: ComponentRepresentation[];
  listUsers?: boolean;
}

export const useUserDataTable = ({
  refresh,
  userStorage,
}: UseUserDataTableProps) => {
  const { adminClient } = useAdminClient();
  const { addAlert, addError } = useAlerts();
  const { t } = useTranslation();
  const { realms, setSearchRealm, accessibleRealms } = useRealms();
  const { realm: realmName } = useRealm();
  const [params] = useSearchParams();

  const [sortingOptions, setSortingOptions] = useState<SortingOptions>();
  const [pagination, setPagination] = useState<PageInfoRepresentation>();
  const [customSelectedRows, setCustomSelectedRows] = useState<
    UserInfoRepresentation[]
  >([]);
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
    return customSelectedRows.map((item) => item.id).filter(isExistGuard);
  }, [customSelectedRows]);

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
      const { "users-info": userInfo, "page-info": pageInfo } =
        response.results;

      return { userInfo, pageInfo };
    },
    [adminClient, customFilters],
  );

  const searchUserWithCustomFilters = (newCustomFilters: CustomUserQuery) => {
    setCustomFilters(newCustomFilters);
    refresh();
    setCustomSelectedRows([]);

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

  const checkUsersForActivity = useCallback(
    (kindOfChecking?: { blocked?: boolean; unverified?: boolean }) => {
      const { blocked, unverified } = kindOfChecking || {};

      const {
        blockedUsers,
        blockedUsernames,
        unlockedUserIds,
        unverifiedUsernames,
        unverifiedUsers,
        verifiedUserIds,
        unlockedAndVerifiedUserIds,
      } = getInvalidUsers(customSelectedRows);

      const isBlockedExist = Boolean(blocked && blockedUsers?.length);
      const isUnverifiedExist = Boolean(unverified && unverifiedUsers?.length);

      if (isBlockedExist) {
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

      if (isUnverifiedExist) {
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

      return {
        isBlockedExist,
        isUnverifiedExist,
        isBlockedOrUnverifiedExist: isBlockedExist || isUnverifiedExist,
        unlockedUserIds,
        verifiedUserIds,
        unlockedAndVerifiedUserIds,
      };
    },
    [customSelectedRows],
  );

  const checkIsUsersNotSelected = useCallback(() => {
    if (!customSelectedRows.length) {
      addError(t("noUsersSelected"), "error");

      return true;
    }

    return false;
  }, [customSelectedRows]);

  const [currentAction, setCurrentAction] =
    useState<CustomUsersAction | null>();

  const dialogProps = useMemo<ConfirmDialogProps>(() => {
    const confirmDialogBaseProps = {
      titleKey: `${currentAction?.type}ConfirmUsers`,
      messageKey: t(`${currentAction?.type}ConfirmDialog`, {
        count: customSelectedRows.length,
      }),
      continueButtonLabel: currentAction?.type,
      continueButtonVariant: ButtonVariant.danger,
    };

    switch (currentAction?.type) {
      case CustomUserToolbarAction.SEND_LOGIN: {
        return {
          ...confirmDialogBaseProps,
          onConfirm: async () => {
            try {
              if (
                checkIsUsersNotSelected() ||
                checkUsersForActivity({ blocked: true, unverified: true })
                  .isBlockedOrUnverifiedExist
              ) {
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
          },
        };
      }

      case CustomUserToolbarAction.SEND_LOGIN_AND_RESET_PASSWORD: {
        return {
          ...confirmDialogBaseProps,
          onConfirm: async () => {
            try {
              if (
                checkIsUsersNotSelected() ||
                checkUsersForActivity({ blocked: true, unverified: true })
                  .isBlockedOrUnverifiedExist
              ) {
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
          },
        };
      }

      case CustomUserToolbarAction.DOWNLOAD_TEMPLATE_CSV: {
        return {
          ...confirmDialogBaseProps,
          onConfirm: async () => {
            try {
              const downloadedFile =
                await adminClient.customUsers.downloadCSVTemplate({
                  realm: filterSearchRealm,
                });

              saveAs(
                addBomAndConvertToBlob(downloadedFile),
                `user_template.csv`,
              );
            } catch (error) {
              addError(t("userCSVTemplateDownloadError"), error);
            }
          },
        };
      }

      case CustomUserToolbarAction.DOWNLOAD_TEMPLATE_EXCEL: {
        return {
          ...confirmDialogBaseProps,
          onConfirm: async () => {
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
          },
        };
      }

      case CustomUserToolbarAction.IMPORT_FILE: {
        return {
          ...confirmDialogBaseProps,
          onConfirm: async () => {
            try {
              const { payload } = currentAction;
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
          },
        };
      }

      case CustomUserToolbarAction.EXPORT_CSV:
      case CustomUserToolbarAction.EXPORT_EXCEL: {
        return {
          ...confirmDialogBaseProps,
          onConfirm: async () => {
            const isExcel =
              currentAction.type === CustomUserToolbarAction.EXPORT_EXCEL;
            const downloadedExtension = isExcel ? "xlsx" : "csv";

            try {
              const downloadedFile =
                await adminClient.customUsers.downloadUsers(
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
          },
        };
      }

      case CustomUserToolbarAction.RESET_PASSWORD: {
        return {
          ...confirmDialogBaseProps,
          onConfirm: async () => {
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
          },
        };
      }

      case CustomUserToolbarAction.BLOCK_USERS: {
        return {
          ...confirmDialogBaseProps,
          onConfirm: async () => {
            try {
              if (checkIsUsersNotSelected()) {
                return;
              }

              const { verifiedUserIds } = checkUsersForActivity({
                unverified: true,
              });

              if (!verifiedUserIds.length) {
                return;
              }

              await adminClient.customUsers.block(
                { realm: realmName },
                verifiedUserIds,
              );
              addAlert(t("userBlockedSuccess"), AlertVariant.success);
              refresh();
            } catch (error) {
              addError(t("userBlockedError"), error);
            }
          },
        };
      }

      case CustomUserToolbarAction.UNLOCK_USERS: {
        return {
          ...confirmDialogBaseProps,
          onConfirm: async () => {
            try {
              if (checkIsUsersNotSelected()) {
                return;
              }

              const { verifiedUserIds } = checkUsersForActivity({
                unverified: true,
              });

              if (!verifiedUserIds.length) {
                return;
              }

              await adminClient.customUsers.unlock(
                { realm: realmName },
                verifiedUserIds,
              );
              addAlert(t("userUnlockedSuccess"), AlertVariant.success);
              refresh();
            } catch (error) {
              addError(t("userUnlockedError"), error);
            }
          },
        };
      }

      default:
        return confirmDialogBaseProps;
    }
  }, [currentAction]);

  const [toggleConfirmActionDialog, ConfirmAction] = useConfirmDialog({
    ...dialogProps,
    onCancel: () => {
      setCurrentAction(null);
    },
  });

  const handleCustomAction = async (action: CustomUsersAction) => {
    switch (action.type) {
      case CustomUserToolbarAction.SEND_LOGIN:
      case CustomUserToolbarAction.SEND_LOGIN_AND_RESET_PASSWORD:
      case CustomUserToolbarAction.RESET_PASSWORD:
      case CustomUserToolbarAction.BLOCK_USERS:
      case CustomUserToolbarAction.UNLOCK_USERS: {
        if (checkIsUsersNotSelected()) {
          return;
        }

        break;
      }

      default:
        break;
    }

    setCurrentAction(action);
    toggleConfirmActionDialog();
  };

  const [toggleUploadUserInfo, UploadUserInfo] = useConfirmDialog({
    titleKey: "information",
    messageKey: t("importUsersMessage"),
    continueButtonLabel: "ok",
    noCancelButton: true,
    onConfirm: () => {
      return;
    },
  });

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

      const { userInfo, pageInfo } = await getUsers({
        first,
        max,
        sortAsc: newSortingOptions?.order === "asc",
        sortField: newSortingOptions?.orderBy,
      });
      const usersNew: UserInfoWithFlatUserPosts[] = [];

      userInfo.forEach((user) => {
        if (!user.userPosts?.length) {
          usersNew.push(user);
        }

        user.userPosts?.forEach((userPost) => {
          const { id, ...userPostRest } = userPost;
          usersNew.push({ ...userPostRest, ...user, userPostId: id });
        });
      });
      setPagination(pageInfo);

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
    customSelectedRows,
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
    setCustomSelectedRows,
    pagination,
    ConfirmAction,
  };
};
