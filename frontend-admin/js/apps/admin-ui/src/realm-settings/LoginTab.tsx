import type RealmRepresentation from "@keycloak/keycloak-admin-client/lib/defs/realmRepresentation";
import { FormGroup, PageSection, Switch } from "@patternfly/react-core";
import { useTranslation } from "react-i18next";
import { FormPanel, HelpItem } from "@keycloak/keycloak-ui-shared";
import { useAdminClient } from "../admin-client";
import { useAlerts } from "../components/alert/Alerts";
import { FormAccess } from "../components/form/FormAccess";
import { useRealm } from "../context/realm-context/RealmContext";
import { useForm, FieldPath } from "react-hook-form";
import { useCustomConfig } from "../customLogic/context/CustomConfigContext";

type RealmSettingsLoginTabProps = {
  realm: RealmRepresentation;
  refresh: () => void;
};

type SwitchType = Partial<{ [K in FieldPath<RealmRepresentation>]: boolean }>;

export const RealmSettingsLoginTab = ({
  realm,
  refresh,
}: RealmSettingsLoginTabProps) => {
  const { adminClient } = useAdminClient();

  const { t } = useTranslation();

  const { addAlert, addError } = useAlerts();
  const { realm: realmName } = useRealm();
  const { setValue, getValues, reset } = useForm<RealmRepresentation>();
  const { isCustomTheme } = useCustomConfig();

  const updateSwitchValue = async (switches: SwitchType | SwitchType[]) => {
    const name = Array.isArray(switches)
      ? Object.keys(switches[0])[0]
      : Object.keys(switches)[0];

    const setValueFromSwitch = (switchItem: SwitchType) => {
      Object.entries(switchItem).map(([key, value]) => {
        setValue(key as FieldPath<RealmRepresentation>, value);
      });
    };

    if (Array.isArray(switches)) {
      switches.map(setValueFromSwitch);
    } else {
      setValueFromSwitch(switches);
    }

    try {
      await adminClient.realms.update(
        {
          realm: realmName,
        },
        getValues(),
      );
      addAlert(t("enableSwitchSuccess", { switch: t(name) }));
      refresh();
    } catch (error) {
      addError(t("enableSwitchError"), error);
    } finally {
      reset();
    }
  };

  return (
    <PageSection variant="light">
      <FormPanel
        className="kc-login-screen"
        title={t("loginScreenCustomization")}
      >
        <FormAccess isHorizontal role="manage-realm">
          <FormGroup
            label={t("registrationAllowed")}
            fieldId="kc-user-reg"
            labelIcon={
              <HelpItem
                helpText={t("userRegistrationHelpText")}
                fieldLabelId="registrationAllowed"
              />
            }
            hasNoPaddingTop
          >
            <Switch
              id="kc-user-reg-switch"
              data-testid="user-reg-switch"
              value={realm.registrationAllowed ? "on" : "off"}
              label={t("on")}
              labelOff={t("off")}
              isChecked={realm.registrationAllowed}
              onChange={(_event, value) => {
                updateSwitchValue({ registrationAllowed: value });
              }}
              aria-label={t("registrationAllowed")}
            />
          </FormGroup>
          <FormGroup
            label={t("resetPasswordAllowed")}
            fieldId="kc-forgot-pw"
            labelIcon={
              <HelpItem
                helpText={t("forgotPasswordHelpText")}
                fieldLabelId="resetPasswordAllowed"
              />
            }
            hasNoPaddingTop
          >
            <Switch
              id="kc-forgot-pw-switch"
              data-testid="forgot-pw-switch"
              name="resetPasswordAllowed"
              value={realm.resetPasswordAllowed ? "on" : "off"}
              label={t("on")}
              labelOff={t("off")}
              isChecked={realm.resetPasswordAllowed}
              onChange={(_event, value) => {
                updateSwitchValue({ resetPasswordAllowed: value });
              }}
              aria-label={t("resetPasswordAllowed")}
            />
          </FormGroup>
          <FormGroup
            label={t("rememberMe")}
            fieldId="kc-remember-me"
            labelIcon={
              <HelpItem
                helpText={t("rememberMeHelpText")}
                fieldLabelId="rememberMe"
              />
            }
            hasNoPaddingTop
          >
            <Switch
              id="kc-remember-me-switch"
              data-testid="remember-me-switch"
              value={realm.rememberMe ? "on" : "off"}
              label={t("on")}
              labelOff={t("off")}
              isChecked={realm.rememberMe}
              onChange={(_event, value) => {
                updateSwitchValue({ rememberMe: value });
              }}
              aria-label={t("rememberMe")}
            />
          </FormGroup>
          {isCustomTheme && (
            <>
              <FormGroup
                label={t("attributes.registrationOnlyInFrame")}
                fieldId="kc-attributes-registration-only-in-frame"
                labelIcon={
                  <HelpItem
                    helpText={t("attributes.registrationOnlyInFrameHelp")}
                    fieldLabelId="attributes.registrationOnlyInFrame"
                  />
                }
                hasNoPaddingTop
              >
                <Switch
                  id="kc-attributes-registration-only-in-frame-switch"
                  data-testid="attributes-registration-only-in-frame-switch"
                  value={
                    realm.attributes?.registrationOnlyInFrame === "true"
                      ? "on"
                      : "off"
                  }
                  label={t("on")}
                  labelOff={t("off")}
                  isChecked={
                    realm.attributes?.registrationOnlyInFrame === "true"
                  }
                  onChange={(_event, value) => {
                    updateSwitchValue({
                      "attributes.registrationOnlyInFrame": value,
                    });
                  }}
                  aria-label={t("attributes.registrationOnlyInFrame")}
                />
              </FormGroup>
              <FormGroup
                label={t("attributes.hideChat")}
                fieldId="kc-attributes-hide-chat"
                labelIcon={
                  <HelpItem
                    helpText={t("attributes.hideChatHelp")}
                    fieldLabelId="attributes.hideChat"
                  />
                }
                hasNoPaddingTop
              >
                <Switch
                  id="kc-attributes-hide-chat-switch"
                  data-testid="attributes-hide-chat-switch"
                  value={realm.attributes?.hideChat !== "true" ? "on" : "off"}
                  label={t("on")}
                  labelOff={t("off")}
                  isChecked={realm.attributes?.hideChat !== "true"}
                  onChange={(_event, value) => {
                    updateSwitchValue({ "attributes.hideChat": !value });
                  }}
                  aria-label={t("attributes.hideChat")}
                />
              </FormGroup>
            </>
          )}
        </FormAccess>
      </FormPanel>
      <FormPanel className="kc-email-settings" title={t("emailSettings")}>
        <FormAccess isHorizontal role="manage-realm">
          <FormGroup
            label={t("registrationEmailAsUsername")}
            fieldId="kc-email-as-username"
            labelIcon={
              <HelpItem
                helpText={t("emailAsUsernameHelpText")}
                fieldLabelId="registrationEmailAsUsername"
              />
            }
            hasNoPaddingTop
          >
            <Switch
              id="kc-email-as-username-switch"
              data-testid="email-as-username-switch"
              value={realm.registrationEmailAsUsername ? "on" : "off"}
              label={t("on")}
              labelOff={t("off")}
              isChecked={realm.registrationEmailAsUsername}
              isDisabled={!isCustomTheme && !realm.registrationAllowed}
              onChange={(_event, value) => {
                updateSwitchValue([
                  {
                    registrationEmailAsUsername: value,
                  },
                  {
                    duplicateEmailsAllowed: false,
                  },
                ]);
              }}
              aria-label={t("registrationEmailAsUsername")}
            />
          </FormGroup>
          <FormGroup
            label={t("loginWithEmailAllowed")}
            fieldId="kc-login-with-email"
            labelIcon={
              <HelpItem
                helpText={t("loginWithEmailHelpText")}
                fieldLabelId="loginWithEmailAllowed"
              />
            }
            hasNoPaddingTop
          >
            <Switch
              id="kc-login-with-email-switch"
              data-testid="login-with-email-switch"
              value={realm.loginWithEmailAllowed ? "on" : "off"}
              label={t("on")}
              labelOff={t("off")}
              isChecked={realm.loginWithEmailAllowed}
              onChange={(_event, value) => {
                updateSwitchValue([
                  {
                    loginWithEmailAllowed: value,
                  },
                  { duplicateEmailsAllowed: false },
                ]);
              }}
              aria-label={t("loginWithEmailAllowed")}
            />
          </FormGroup>
          <FormGroup
            label={t("duplicateEmailsAllowed")}
            fieldId="kc-duplicate-emails"
            labelIcon={
              <HelpItem
                helpText={t("duplicateEmailsHelpText")}
                fieldLabelId="duplicateEmailsAllowed"
              />
            }
            hasNoPaddingTop
          >
            <Switch
              id="kc-duplicate-emails-switch"
              data-testid="duplicate-emails-switch"
              label={t("on")}
              labelOff={t("off")}
              isChecked={realm.duplicateEmailsAllowed}
              onChange={(_event, value) => {
                updateSwitchValue({
                  duplicateEmailsAllowed: value,
                });
              }}
              isDisabled={
                realm.loginWithEmailAllowed || realm.registrationEmailAsUsername
              }
              aria-label={t("duplicateEmailsAllowed")}
            />
          </FormGroup>
          <FormGroup
            label={t("verifyEmail")}
            fieldId="kc-verify-email"
            labelIcon={
              <HelpItem
                helpText={t("verifyEmailHelpText")}
                fieldLabelId="verifyEmail"
              />
            }
            hasNoPaddingTop
          >
            <Switch
              id="kc-verify-email-switch"
              data-testid="verify-email-switch"
              name="verifyEmail"
              value={realm.verifyEmail ? "on" : "off"}
              label={t("on")}
              labelOff={t("off")}
              isChecked={realm.verifyEmail}
              onChange={(_event, value) => {
                updateSwitchValue({ verifyEmail: value });
              }}
              aria-label={t("verifyEmail")}
            />
          </FormGroup>
        </FormAccess>
      </FormPanel>
      <FormPanel
        className="kc-user-info-settings"
        title={t("userInfoSettings")}
      >
        <FormAccess isHorizontal role="manage-realm">
          <FormGroup
            label={t("editUsernameAllowed")}
            fieldId="kc-edit-username"
            labelIcon={
              <HelpItem
                helpText={t("editUsernameHelp")}
                fieldLabelId="editUsernameAllowed"
              />
            }
            hasNoPaddingTop
          >
            <Switch
              id="kc-edit-username-switch"
              data-testid="edit-username-switch"
              value={realm.editUsernameAllowed ? "on" : "off"}
              label={t("on")}
              labelOff={t("off")}
              isChecked={realm.editUsernameAllowed}
              onChange={(_event, value) => {
                updateSwitchValue({ editUsernameAllowed: value });
              }}
              aria-label={t("editUsernameAllowed")}
            />
          </FormGroup>
        </FormAccess>
      </FormPanel>
      {isCustomTheme && (
        <FormPanel className="kc-other-settings" title={t("otherSettings")}>
          <FormAccess isHorizontal role="manage-realm">
            <FormGroup
              label={t("attributes.checkInRiasIfNotFound")}
              fieldId="kc-attributes-check-in-rias-if-not-found"
              labelIcon={
                <HelpItem
                  helpText={t("attributes.checkInRiasIfNotFoundHelp")}
                  fieldLabelId="attributes.checkInRiasIfNotFound"
                />
              }
              hasNoPaddingTop
            >
              <Switch
                id="kc-attributes-check-in-rias-if-not-found-switch"
                data-testid="attributes-check-in-rias-if-not-found-switch"
                value={
                  realm.attributes?.checkInRiasIfNotFound === "true"
                    ? "on"
                    : "off"
                }
                label={t("on")}
                labelOff={t("off")}
                isChecked={realm.attributes?.checkInRiasIfNotFound === "true"}
                onChange={(_event, value) => {
                  updateSwitchValue({
                    "attributes.checkInRiasIfNotFound": value,
                  });
                }}
                aria-label={t("attributes.checkInRiasIfNotFound")}
              />
            </FormGroup>
            <FormGroup
              label={t("attributes.realmInSchedule")}
              fieldId="kc-attributes-realm-in-schedule"
              labelIcon={
                <HelpItem
                  helpText={t("attributes.realmInScheduleHelp")}
                  fieldLabelId="attributes.realmInSchedule"
                />
              }
              hasNoPaddingTop
            >
              <Switch
                id="kc-attributes-realm-in-schedule-switch"
                data-testid="attributes-realm-in-schedule-switch"
                value={
                  realm.attributes?.realmInSchedule === "true" ? "on" : "off"
                }
                label={t("on")}
                labelOff={t("off")}
                isChecked={realm.attributes?.realmInSchedule === "true"}
                onChange={(_event, value) => {
                  updateSwitchValue({ "attributes.realmInSchedule": value });
                }}
                aria-label={t("attributes.realmInSchedule")}
              />
            </FormGroup>
          </FormAccess>
        </FormPanel>
      )}
    </PageSection>
  );
};
