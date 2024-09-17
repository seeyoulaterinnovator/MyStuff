import type ComponentRepresentation from "@keycloak/keycloak-admin-client/lib/defs/componentRepresentation";
import type { CustomUserQuery } from "@keycloak/keycloak-admin-client/lib/resources/custom/users";
import type UserRepresentation from "@keycloak/keycloak-admin-client/lib/defs/userRepresentation";
import type { UserInfoRepresentation } from "@keycloak/keycloak-admin-client/lib/defs/custom/userRepresentation";
import { AlertVariant, Button, Checkbox } from "@patternfly/react-core";
import { useCallback, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { CustomUserToolbarAction } from "../constants/user";
import { useAdminClient } from "../../admin-client";
import { useAlerts } from "../../components/alert/Alerts";
import { useRealm } from "../../context/realm-context/RealmContext";
import { useRealms } from "../../context/RealmsContext";
import { toUser } from "../../user/routes/User";
import { AdminTheme } from "../constants/theme";
import {
  KeycloakDataTable,
  type DetailField,
  type Field,
} from "../../components/table-toolbar/KeycloakDataTable";
import { emptyFormatter } from "../../util";
import { useTranslation } from "react-i18next";
import { saveAs } from "file-saver";
import { useConfirmDialog } from "../../components/confirm-dialog/ConfirmDialog";
import { isExistGuard } from "../helpers/guards";
import type { CustomUsersActions } from "../types/users";

const getBlockedUsers = (
  users?: Array<UserRepresentation | UserInfoRepresentation>,
) => {
  const blockedUsers = users?.filter((item) => !item.enabled);
  const blockedUsernames = users?.map((item) => item.username).join(", ");

  return { blockedUsers, blockedUsernames };
};

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
  const navigate = useNavigate();
  const { adminClient } = useAdminClient();
  const { addAlert, addError } = useAlerts();
  const { t } = useTranslation();
  const { realms } = useRealms();
  const { realm: realmName, realmRepresentation: realm } = useRealm();
  const [customFilters, setCustomFilters] = useState<CustomUserQuery>({
    searchRealm: realmName,
  });

  const isCustomTheme = realm?.adminTheme === AdminTheme.KEYCLOAK_V2;
  //should *only* list users when no user federation is configured
  const listUsers = !(userStorage && userStorage.length > 0);

  const selectedIds = useMemo(() => {
    return selectedRows.map((item) => item.id).filter(isExistGuard);
  }, [selectedRows]);

  const userColumns = useMemo(() => {
    return [
      {
        name: "id",
        displayKey: "id",
        cellProps: {
          style: {
            display: "block",
          },
        },
        cellRenderer: (row) => {
          return (
            <Button
              variant="link"
              style={{
                width: "200px",
                maxWidth: "200px",
                overflow: "hidden",
                textOverflow: "ellipsis",
              }}
              onClick={() => {
                navigate(
                  toUser({
                    id: row.id,
                    realm: realmName,
                    tab: "settings",
                  }),
                );
              }}
            >
              {row.id}
            </Button>
          );
        },
      },
      {
        name: "email",
        displayKey: "email",
      },
      {
        name: "firstName",
        displayKey: "firstName",
        cellFormatters: [emptyFormatter()],
      },
      {
        name: "enabled",
        displayKey: "enabled",
        cellRenderer: (row) => {
          return <Checkbox id="dd" checked={row.enabled} isDisabled />;
        },
      },
      {
        name: "phone",
        displayKey: "phone",
        cellFormatters: [emptyFormatter()],
      },
    ] satisfies Field<UserInfoRepresentation>[];
  }, []);

  const userDetailColumns = useMemo(() => {
    return [
      {
        name: "userPosts",
        enabled: (rowUser) => !!rowUser.userPosts?.length,
        cellProps: {
          style: {
            display: "block",
          },
        },
        cellRenderer: (row) => (
          <KeycloakDataTable
            loader={row.userPosts}
            ariaLabelKey="userPosts"
            isStriped
            onlyTable
            columns={[
              {
                name: "organization",
                displayKey: "organization",
              },
              {
                name: "tomsId",
                displayKey: "tomsId",
              },
              {
                name: "userRole",
                displayKey: "roles",
                cellRenderer: (row) => row.userRole.name,
              },
              {
                name: "systemRoles",
                displayKey: "systems",
                cellRenderer: (row) => {
                  return (
                    <p style={{ whiteSpace: "pre-wrap" }}>
                      {row.systemRoles
                        .map((systemRole) => systemRole.externalSystem.name)
                        .join("\n")}
                    </p>
                  );
                },
              },
            ]}
          />
        ),
      },
    ] satisfies Array<DetailField<UserInfoRepresentation>>;
  }, []);

  const getUsers = useCallback(
    async (query: CustomUserQuery) => {
      const response = await adminClient.customUsers.findUsers({
        ...{
          ...customFilters,
          ...query,
        },
      });
      const { "page-info": pageInfo, "users-info": userInfo } =
        response.results;

      return userInfo;
    },
    [adminClient],
  );

  const searchUserWithCustomFilters = (newCustomFilters: CustomUserQuery) => {
    setCustomFilters(newCustomFilters);
    refresh();
  };

  const checkIsSelectedUsersBlocked = useCallback(() => {
    const { blockedUsers, blockedUsernames } = getBlockedUsers(selectedRows);

    if (blockedUsers?.length) {
      addError(
        t("blockedUserSelected", { username: blockedUsernames }),
        "error",
      );
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

  const handleCustomAction = async (action: CustomUsersActions) => {
    const { type } = action;

    switch (action.type) {
      case CustomUserToolbarAction.SEND_LOGIN: {
        try {
          if (checkIsUsersNotSelected() || checkIsSelectedUsersBlocked()) {
            return;
          }

          await adminClient.customUsers.sendLogin(
            { realm: realmName },
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
          if (checkIsUsersNotSelected() || checkIsSelectedUsersBlocked()) {
            return;
          }

          await adminClient.customUsers.sendLoginAndResetPassword(
            { realm: realmName },
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
              realm: realmName,
            });

          await saveAs(
            new Blob([downloadedFile], { type: "application/octet-stream" }),
            `user_template.csv`,
          );
          addAlert(t("userCSVTemplateDownloadSuccess"), AlertVariant.success);
        } catch (error) {
          addError(t("userCSVTemplateDownloadError"), error);
        }

        break;
      }

      case CustomUserToolbarAction.DOWNLOAD_TEMPLATE_EXCEL: {
        try {
          const downloadedFile =
            await adminClient.customUsers.downloadExcelTemplate({
              realm: realmName,
            });
          saveAs(
            new Blob([downloadedFile], { type: "application/octet-stream" }),
            `user_template.xlsx`,
          );
          addAlert(t("userExcelTemplateDownloadSuccess"), AlertVariant.success);
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
              { realm: realmName },
              formData,
            );

            toggleUploadUserInfo();
          }
        } catch (error: any) {
          if ("status" in error) {
            if (error.status === 400) {
              addError(error.data.message, error);
            } else if (error.status === 502) {
              addAlert(t("tooManyUsersToImport"), AlertVariant.info);
            } else {
              addError(error.statusText, error);
            }
          }
        }

        break;
      }

      case CustomUserToolbarAction.EXPORT_CSV:
      case CustomUserToolbarAction.EXPORT_EXCEL: {
        try {
          const downloadedFile = await adminClient.customUsers.downloadUsers(
            { realm: realmName },
            {
              type:
                type === CustomUserToolbarAction.EXPORT_CSV ? "csv" : "xlsx",
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
            new Blob([downloadedFile], { type: "application/octet-stream" }),
            `user_info.${CustomUserToolbarAction.EXPORT_CSV ? "csv" : "xlsx"}`,
          );
          addAlert(t("usersExportedSuccess"), AlertVariant.success);
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
    onConfirm: () => {},
  });

  const customLoader = async (first?: number, max?: number) => {
    if (!listUsers) {
      return []
    }

    try {
      return await getUsers({
        first,
        max,
      });
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
    customFilters,
    isCustomTheme,
    realms,
    userColumns,
    userDetailColumns,
    customLoader,
    getUsers,
    handleCustomAction,
    searchUserWithCustomFilters,
    setCustomFilters,
    toggleUploadUserInfo,
    UploadUserInfo,
  };
};
