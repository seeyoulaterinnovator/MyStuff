import { useAdminClient } from "../admin-client";
import {
  Field,
  KeycloakDataTable,
} from "../components/table-toolbar/KeycloakDataTable";
import { ListEmptyState } from "../components/list-empty-state/ListEmptyState";
import { useTranslation } from "react-i18next";
import { useRealm } from "../context/realm-context/RealmContext";
import {
  CustomSettingRepresentation,
  CustomSettingType,
} from "@keycloak/keycloak-admin-client/lib/defs/custom/customSettingRepresentation";
import {
  AlertVariant,
  Button,
  SelectOption,
  TextInput,
} from "@patternfly/react-core";
import { MutableRefObject, useMemo, useRef, useState } from "react";
import { useAlerts } from "../components/alert/Alerts";
import TimeUnit from "@keycloak/keycloak-admin-client/lib/defs/custom/timeUnits";
import { KeycloakSelect, SelectVariant } from "@keycloak/keycloak-ui-shared";

type CustomSettingsTableProps = {
  type: CustomSettingType;
};

type IdValueMap = Record<string, string>;
type IdUnitMap = Record<string, TimeUnit>;

const UnitSelect = (props: {
  defaultValue?: TimeUnit;
  onChange: (value: TimeUnit) => void;
}) => {
  const { defaultValue, onChange } = props;
  const { t } = useTranslation();
  const [open, setOpen] = useState(false);
  const [value, setValue] = useState(defaultValue);
  return (
    <KeycloakSelect
      onToggle={() => setOpen(!open)}
      onSelect={(value) => {
        setValue(value as TimeUnit);
        setOpen(false);
        onChange(value as TimeUnit);
      }}
      selections={value}
      variant={SelectVariant.single}
      isOpen={open}
      placeholderText={t("selectTimeUnit")}
      aria-label={t("selectTimeUnit")}
    >
      <SelectOption value="SECONDS">{t("seconds")}</SelectOption>
      <SelectOption value="MINUTES">{t("minutes")}</SelectOption>
      <SelectOption value="HOURS">{t("hours")}</SelectOption>
      <SelectOption value="DAYS">{t("days")}</SelectOption>
    </KeycloakSelect>
  );
};

type SaveButtonProps = {
  setting: CustomSettingRepresentation;
  valuesRef: MutableRefObject<IdValueMap>;
  unitsRef: MutableRefObject<IdUnitMap>;
};

const SaveButton = (props: SaveButtonProps) => {
  const { setting, valuesRef, unitsRef } = props;

  const { t } = useTranslation();
  const { realm } = useRealm();
  const { adminClient } = useAdminClient();
  const { addAlert, addError } = useAlerts();
  const [isChanging, setIsChanging] = useState(false);

  return (
    <Button
      style={{ float: "right" }}
      type="button"
      isDisabled={isChanging}
      isLoading={isChanging}
      onClick={async () => {
        const value = valuesRef.current[setting.id] ?? setting.value;
        const unit = unitsRef.current[setting.id] ?? setting.unit;
        setIsChanging(true);
        try {
          await adminClient.customSettings.updateSetting(
            {
              realm,
              settingId: setting.id,
            },
            {
              ...setting,
              value,
              unit,
            },
          );
          addAlert(
            t("saveCustomSettingSuccess", {
              name: setting.name,
            }),
            AlertVariant.success,
          );
        } catch (error) {
          addError("saveCustomSettingError", error);
        } finally {
          setIsChanging(false);
        }
      }}
    >
      {t("save")}
    </Button>
  );
};

export default function CustomSettingsTable(props: CustomSettingsTableProps) {
  const { type } = props;
  const { t } = useTranslation();
  const { realm } = useRealm();
  const { adminClient } = useAdminClient();
  const valuesRef = useRef<IdValueMap>({});
  const unitsRef = useRef<IdUnitMap>({});

  const loader = async () => {
    valuesRef.current = {};
    unitsRef.current = {};
    const response = await adminClient.customSettings.findSettings({
      realm,
      type,
    });
    return response.results.settings;
  };

  const columns = useMemo<Field<CustomSettingRepresentation>[]>(
    () =>
      [
        {
          name: "name",
          displayKey: "customSettingName",
        },
        {
          name: "value",
          displayKey: "customSettingValue",
          cellRenderer: (setting: CustomSettingRepresentation) => {
            return (
              <TextInput
                aria-label={`Setting ${setting.id}`}
                defaultValue={setting.value}
                onChange={(_event, value) => {
                  valuesRef.current = {
                    ...valuesRef.current,
                    [setting.id]: value,
                  };
                }}
              />
            );
          },
        },
      ]
        .concat(
          type === "REALM"
            ? [
                {
                  name: "unit",
                  displayKey: "timeUnit",
                  cellRenderer: (setting: CustomSettingRepresentation) => {
                    return (
                      <UnitSelect
                        defaultValue={setting.unit}
                        onChange={(unit) => {
                          unitsRef.current = {
                            ...unitsRef.current,
                            [setting.id]: unit,
                          };
                        }}
                      />
                    );
                  },
                },
              ]
            : [],
        )
        .concat([
          {
            name: "desc",
            displayKey: "description",
          },
          {
            name: "",
            displayKey: " ",
            cellRenderer: (setting) => (
              <SaveButton
                setting={setting}
                valuesRef={valuesRef}
                unitsRef={unitsRef}
              />
            ),
          },
        ]),
    [type],
  );

  return (
    <KeycloakDataTable
      loader={loader}
      ariaLabelKey="titleCustomSettings"
      columns={columns}
      emptyState={
        <ListEmptyState
          message={t("emptyCustomSettings")}
          instructions={t("emptyCustomSettingsInstructions")}
        />
      }
    />
  );
}
