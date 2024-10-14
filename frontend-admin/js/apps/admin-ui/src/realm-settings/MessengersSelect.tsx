import { useState } from "react";
import type { MessengerRepresentation } from "@keycloak/keycloak-admin-client/lib/defs/custom/messengerRepresentation";
import { useFetch } from "../utils/useFetch";
import { FormGroup, SelectOption } from "@patternfly/react-core";
import {
  HelpItem,
  KeycloakSelect,
  SelectVariant,
} from "@keycloak/keycloak-ui-shared";
import { Controller, useFormContext } from "react-hook-form";
import { useAdminClient } from "../admin-client";
import { useTranslation } from "react-i18next";
import { useAlerts } from "../components/alert/Alerts";

export const MessengersSelect = () => {
  const { adminClient } = useAdminClient();
  const { t } = useTranslation();
  const { addAlert, addError } = useAlerts();

  const { control } = useFormContext();

  const [messengers, setMessengers] = useState<MessengerRepresentation[]>([]);
  const [isMessengersOpen, setIsMessengersOpen] = useState<boolean>(false);

  useFetch(
    async () => {
      try {
        return await adminClient.customMessenger.getMessengers();
      } catch (error) {
        addError(t("getMessengersError"), error);
      }
    },
    (response) => {
      setMessengers(response?.results.messengers || []);
    },
    [adminClient],
  );

  return (
    <FormGroup
      label={t("messenger")}
      labelIcon={
        <HelpItem
          helpText={t("messengerHelp")}
          fieldLabelId="smtpServer.messenger"
        />
      }
      fieldId="smtpServer.messenger"
    >
      <Controller
        name="smtpServer.messenger"
        control={control}
        defaultValue={""}
        render={({ field }) => {
          const splittedFieldValue = (field.value as string)
            ?.split(",")
            .filter((item) => !!item);

          return (
            <KeycloakSelect
              toggleId="smtpServer.messenger"
              onToggle={setIsMessengersOpen}
              onSelect={(selectedValue) => {
                const option = selectedValue.toString();
                const changedValue = splittedFieldValue.includes(option)
                  ? splittedFieldValue.filter((item) => item !== option)
                  : [...splittedFieldValue, option];

                field.onChange(changedValue.join(","));
                setIsMessengersOpen(true);
                addAlert(t("messengerReminder"));
              }}
              typeAheadAriaLabel="Select"
              chipGroupProps={{
                numChips: 3,
                expandedText: t("hide"),
                collapsedText: t("showRemaining"),
              }}
              selections={splittedFieldValue}
              variant={SelectVariant.typeaheadMulti}
              aria-label={t("messenger")}
              isOpen={isMessengersOpen}
            >
              {messengers?.map((option) => (
                <SelectOption
                  hasCheckbox
                  isSelected={splittedFieldValue?.includes(option.name)}
                  key={option.name}
                  value={option.name}
                >
                  {option.name}
                </SelectOption>
              ))}
            </KeycloakSelect>
          );
        }}
      />
    </FormGroup>
  );
};
