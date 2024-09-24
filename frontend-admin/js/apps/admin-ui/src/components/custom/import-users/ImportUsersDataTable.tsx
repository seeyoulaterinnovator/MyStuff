import {
  UserReportStatus,
  type ImportUsersReportRepresentation,
} from "@keycloak/keycloak-admin-client/lib/defs/custom/userRepresentation";
import { NetworkError } from "@keycloak/keycloak-admin-client/lib";
import { KeycloakDataTable } from "../../table-toolbar/KeycloakDataTable";
import { useTranslation } from "react-i18next";
import { useAdminClient } from "../../../admin-client";
import { useAlerts } from "../../alert/Alerts";
import { useRealm } from "../../../context/realm-context/RealmContext";
import { ImportUsersDataTableToolbarItems } from "./ImportUsersDataTableToolbarItems";
import type { CustomImportUsersAction } from "../../../customLogic/types/users";
import { CustomImportUsersToolbarAction } from "../../../customLogic/constants/user";
import { AlertVariant, Toolbar, ToolbarContent } from "@patternfly/react-core";
import { useCallback, useEffect, useState } from "react";
import { cellWidth, type IRowData } from "@patternfly/react-table";
import { saveAs } from "file-saver";
import { ListEmptyState } from "../../list-empty-state/ListEmptyState";
import { isEqual } from "lodash-es";

let importUsersInterval: NodeJS.Timeout | null = null;

const resetImportUsersInterval = () => {
  if (importUsersInterval) {
    clearInterval(importUsersInterval);
  }
};

export const ImportUsersDataTable = () => {
  const { t } = useTranslation();
  const { adminClient } = useAdminClient();
  // const { realm: realmName } = useRealm();
  const { addAlert, addError } = useAlerts();

  const [isLoading, setIsLoading] = useState(false);
  const [importUsersReportData, setImportUsersReportData] = useState<
    ImportUsersReportRepresentation[]
  >([]);
  const [first, setFirst] = useState<number>();
  const [max, setMax] = useState<number>();

  const loader = useCallback(async () => {
    try {
      const importsResponse = await adminClient.customUsers.getImportsReport({
        first,
        max,
      });

      return importsResponse.results.importUsersReports || [];
    } catch (error) {
      addError("noImportUsersFoundError", error);
      return [];
    }
  }, [adminClient, first, max]);

  const handlePaginationChange = (newFirst?: number, newMax?: number) => {
    setFirst(newFirst);
    setMax(newMax);
  };

  // const toolbar = () => {
  //   return (
  //     <ImportUsersDataTableToolbarItems
  //       onAction={async (action: CustomImportUsersAction) => {
  //         const { type } = action;

  //         switch (type) {
  //           case CustomImportUsersToolbarAction.IMPORT_FILE: {
  //             try {
  //               const { payload } = action;

  //               if (payload) {
  //                 const formData = new FormData();
  //                 formData.append("file", payload);

  //                 await adminClient.customUsers.importFile(payload.name)(
  //                   { realm: realmName },
  //                   formData,
  //                 );

  //                 addAlert(t("usersImportedSuccess"), AlertVariant.success);
  //               }
  //             } catch (error: unknown) {
  //               if (error instanceof NetworkError) {
  //                 switch (error.response.status) {
  //                   case 400:
  //                     addError(error.message, error);
  //                     break;

  //                   case 502:
  //                     addAlert(t("tooManyUsersToImport"), AlertVariant.info);
  //                     break;

  //                   default:
  //                     addError(error.response.statusText, error);
  //                 }
  //               }
  //             }

  //             break;
  //           }

  //           default:
  //             break;
  //         }
  //       }}
  //     />
  //   );
  // };

  useEffect(() => {
    resetImportUsersInterval();
    setIsLoading(true);

    importUsersInterval = setInterval(async () => {
      const dataLoader = await loader();

      setImportUsersReportData((prevData) => {
        return isEqual(prevData, dataLoader) ? prevData : dataLoader;
      });

      setIsLoading(false);
    }, 1000);

    return () => {
      resetImportUsersInterval();
    };
  }, [loader]);

  return (
    <KeycloakDataTable
      loader={importUsersReportData}
      ariaLabelKey="importUsersReports"
      // toolbarItem={toolbar()}
      onPaginationChange={handlePaginationChange}
      withoutRefreshButton
      isPaginated
      isLoading={isLoading}
      emptyState={
        <>
          {/* <Toolbar>
            <ToolbarContent>{toolbar()}</ToolbarContent>
          </Toolbar> */}
          <ListEmptyState
            hasIcon={false}
            message={t("noImportUsersFound")}
            instructions={t("emptyImportUsersInstructions")}
          />
        </>
      }
      actionResolver={(rowData: IRowData) => {
        const importUsersReport: ImportUsersReportRepresentation = rowData.data;

        return [
          {
            title: t("download"),
            onClick: async () => {
              try {
                if (importUsersReport.status !== UserReportStatus.DONE) {
                  addAlert(t("notDoneImportStatusReport"), AlertVariant.info);
                  return;
                }

                const downloadedFile =
                  await adminClient.customUsers.downloadImportUsersReport({
                    id: importUsersReport.id,
                  });

                saveAs(
                  new Blob([downloadedFile], {
                    type: "application/octet-stream",
                  }),
                  `import_users_report.csv`,
                );
              } catch (error) {
                addError("downloadImportUsersReportError", error);
              }
            },
          },
          {
            title: t("activateUsers"),
            onClick: async () => {
              try {
                if (importUsersReport.status !== UserReportStatus.DONE) {
                  addAlert(t("notDoneImportStatusReport"), AlertVariant.info);
                  return;
                }

                await adminClient.customUsers.activeImportUsersReport({
                  id: importUsersReport.id,
                });

                addAlert(t("activateImportUsersReportSuccess"));
              } catch (error) {
                addError("activateImportUsersReportError", error);
              }
            },
          },
        ];
      }}
      columns={[
        {
          name: "id",
          displayKey: "reportId",
          transforms: [cellWidth(20)],
        },
        {
          name: "name",
          displayKey: "fileName",
          transforms: [cellWidth(20)],
        },
        {
          name: "importDate",
          displayKey: "importDate",
          transforms: [cellWidth(15)],
        },
        {
          name: "countImportUsers",
          displayKey: "countImportUsers",
        },
        {
          name: "countCreatedUsers",
          displayKey: "countCreatedUsers",
        },
        {
          name: "countClones",
          displayKey: "countClones",
        },
        {
          name: "status",
          displayKey: "status",
          transforms: [cellWidth(10)],
        },
      ]}
    />
  );
};
