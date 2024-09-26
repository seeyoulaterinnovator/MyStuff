import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Button } from "@patternfly/react-core";
import { KeycloakDataTable } from "../../../components/table-toolbar/KeycloakDataTable";
import { useRealms } from "../../../context/RealmsContext";
import { ListEmptyState } from "../../../components/list-empty-state/ListEmptyState";
import { toDashboard } from "../../../dashboard/routes/Dashboard";

export const RealmsDataTable = () => {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const { accessibleRealms } = useRealms();

  return (
    <KeycloakDataTable
      loader={accessibleRealms}
      ariaLabelKey="realms"
      onlyTable
      emptyState={
        <ListEmptyState
          hasIcon={false}
          message={t("noRealmsFound")}
          instructions=""
        />
      }
      columns={[
        {
          name: "name",
          displayKey: "realm",
          cellRenderer: (row) => {
            return (
              <Button
                variant="link"
                onClick={() => {
                  navigate(toDashboard({ realm: row.name }));
                }}
              >
                {row.displayName || row.name}
              </Button>
            );
          },
        },
      ]}
    />
  );
};
