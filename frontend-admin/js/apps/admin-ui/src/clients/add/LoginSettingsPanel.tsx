import { useFormContext } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { SelectControl, TextAreaControl } from "@keycloak/keycloak-ui-shared";
import { DefaultSwitchControl } from "../../components/SwitchControl";
import { FormAccess } from "../../components/form/FormAccess";
import { useServerInfo } from "../../context/server-info/ServerInfoProvider";
import { convertAttributeNameToForm } from "../../util";
import { FormFields } from "../ClientDetails";
import { useEffect } from "react";

export const LoginSettingsPanel = ({ access }: { access?: boolean }) => {
  const { t } = useTranslation();
  const { watch, setValue } = useFormContext<FormFields>();

  const loginThemes = useServerInfo().themes!["login"];
  const consentRequired = watch("consentRequired");
  const displayOnConsentScreen: string = watch(
    convertAttributeNameToForm<FormFields>(
      "attributes.display.on.consent.screen",
    ),
  );

  const activateNewAuth: string = watch(
    convertAttributeNameToForm<FormFields>("attributes.activateNewAuth"),
  );

  const loginViaEmailOrUsernameAndPassword: string = watch(
    convertAttributeNameToForm<FormFields>(
      "attributes.loginViaEmailOrUsernameAndPassword",
    ),
  );

  const loginViaSms: string = watch(
    convertAttributeNameToForm<FormFields>("attributes.loginViaSms"),
  );

  const loginViaPhoneCall: string = watch(
    convertAttributeNameToForm<FormFields>("attributes.loginViaPhoneCall"),
  );

  useEffect(() => {
    if (loginViaEmailOrUsernameAndPassword === "true") {
      setValue("attributes.loginViaSms", "false");
      setValue("attributes.loginViaPhoneCall", "false");
    }
  }, [loginViaEmailOrUsernameAndPassword]);

  useEffect(() => {
    if (loginViaSms === "true") {
      setValue("attributes.loginViaEmailOrUsernameAndPassword", "false");
      setValue("attributes.loginViaPhoneCall", "false");
    }
  }, [loginViaSms]);

  useEffect(() => {
    if (loginViaPhoneCall === "true") {
      setValue("attributes.loginViaEmailOrUsernameAndPassword", "false");
      setValue("attributes.loginViaSms", "false");
    }
  }, [loginViaPhoneCall]);

  useEffect(() => {
    if (activateNewAuth !== "true") {
      setValue("attributes.loginViaEmailOrUsernameAndPassword", "false");
      setValue("attributes.loginViaSms", "false");
      setValue("attributes.loginViaPhoneCall", "false");
    }
  }, [activateNewAuth]);

  return (
    <FormAccess isHorizontal fineGrainedAccess={access} role="manage-clients">
      <SelectControl
        name="attributes.login_theme"
        label={t("loginTheme")}
        labelIcon={t("loginThemeHelp")}
        controller={{
          defaultValue: "",
        }}
        options={[
          { key: "", value: t("choose") },
          ...loginThemes.map(({ name }) => ({ key: name, value: name })),
        ]}
      />
      <DefaultSwitchControl
        name="consentRequired"
        label={t("consentRequired")}
        labelIcon={t("consentRequiredHelp")}
      />
      <DefaultSwitchControl
        name={convertAttributeNameToForm<FormFields>(
          "attributes.display.on.consent.screen",
        )}
        label={t("displayOnClient")}
        labelIcon={t("displayOnClientHelp")}
        isDisabled={!consentRequired}
        stringify
      />
      <TextAreaControl
        name={convertAttributeNameToForm<FormFields>(
          "attributes.consent.screen.text",
        )}
        label={t("consentScreenText")}
        labelIcon={t("consentScreenTextHelp")}
        isDisabled={!(consentRequired && displayOnConsentScreen === "true")}
      />
      <DefaultSwitchControl
        name={convertAttributeNameToForm<FormFields>(
          "attributes.activateNewAuth",
        )}
        label={t("attributes.activateNewAuth")}
        stringify
      />
      {activateNewAuth === "true" && (
        <>
          <DefaultSwitchControl
            name={convertAttributeNameToForm<FormFields>(
              "attributes.loginViaSms",
            )}
            label={t("attributes.loginViaSms")}
            labelIcon={t("attributes.loginViaSmsHelp")}
            stringify
          />
          <DefaultSwitchControl
            name={convertAttributeNameToForm<FormFields>(
              "attributes.loginViaEmailOrUsernameAndPassword",
            )}
            label={t("attributes.loginViaEmailOrUsernameAndPassword")}
            labelIcon={t("attributes.loginViaEmailOrUsernameAndPasswordHelp")}
            stringify
          />
          <DefaultSwitchControl
            name={convertAttributeNameToForm<FormFields>(
              "attributes.loginViaPhoneCall",
            )}
            label={t("attributes.loginViaPhoneCall")}
            labelIcon={t("attributes.loginViaPhoneCallHelp")}
            stringify
          />
        </>
      )}
    </FormAccess>
  );
};
