import {useAdminClient} from "../admin-client";
import {KeycloakDataTable} from "../components/table-toolbar/KeycloakDataTable";
import {ListEmptyState} from "../components/list-empty-state/ListEmptyState";
import {useTranslation} from "react-i18next";
import {useRealm} from "../context/realm-context/RealmContext";
import {CustomSettingType} from "@keycloak/keycloak-admin-client/lib/defs/customSettingRepresentation";
import {Button, TextInput} from "@patternfly/react-core";
import {useRef} from "react";

type CustomSettingsTableProps = {
  type: CustomSettingType;
};

type IdValueMap = Record<string, string>;

export default function CustomSettingsTable(props: CustomSettingsTableProps) {
  const {
    type
  } = props;
  const {t} = useTranslation();
  const {realm} = useRealm();
  const {adminClient} = useAdminClient();
  const valuesRef = useRef<IdValueMap>({});

  const loader = async() => {
    valuesRef.current = {};
    const response = await adminClient.customSettings.find({
      realm,
      type
    });
    return response.results.settings;
  };

  return (
    <KeycloakDataTable
      loader={loader}
      ariaLabelKey="titleCustomSettings"
      columns={[
        {
          name: "name",
          displayKey: "name"
        }, {
          name: "value",
          displayKey: "value",
          cellRenderer: (setting) => {
            return (
              <TextInput
                aria-label={`Setting ${setting.id}`}
                defaultValue={setting.value}
                onChange={(_event, value) => {
                  valuesRef.current = {
                    ...valuesRef.current,
                    [setting.id]: value
                  }
                }}
              />
            );
          },
        }, {
          name: "desc",
          displayKey: "description"
        }, {
          name: "",
          displayKey: " ",
          cellRenderer: (setting) => {
            return (
              <Button
                style={{float: 'right'}}
                type="button"
                onClick={() => {
                  console.log(`Save settings ${setting.id}: ${valuesRef.current[setting.id]??setting.value}`); // TODO
                }}
              >
                {t("save")}
              </Button>
            );
          }
        }]}
      emptyState={
        <ListEmptyState
          message={t("emptyCustomSettings")}
          instructions={t("emptyCustomSettingsInstructions")}
        />
      }
    />
  )
}
