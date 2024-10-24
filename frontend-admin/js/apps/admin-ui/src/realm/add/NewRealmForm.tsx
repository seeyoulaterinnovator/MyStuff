import type RealmRepresentation from "@keycloak/keycloak-admin-client/lib/defs/realmRepresentation";
import {
  ActionGroup,
  AlertVariant,
  Button,
  PageSection,
} from "@patternfly/react-core";
import { useState } from "react";
import { FormProvider, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { FormSubmitButton, TextControl } from "@keycloak/keycloak-ui-shared";
import { useAdminClient } from "../../admin-client";
import { DefaultSwitchControl } from "../../components/SwitchControl";
import { useAlerts } from "../../components/alert/Alerts";
import { FormAccess } from "../../components/form/FormAccess";
import { JsonFileUpload } from "../../components/json-file-upload/JsonFileUpload";
import { ViewHeader } from "../../components/view-header/ViewHeader";
import { useRealms } from "../../context/RealmsContext";
import { useWhoAmI } from "../../context/whoami/WhoAmI";
import { toDashboard } from "../../dashboard/routes/Dashboard";
import { convertFormValuesToObject } from "../../util";

export default function NewRealmForm() {
  const { adminClient } = useAdminClient();

  const { t } = useTranslation();
  const navigate = useNavigate();
  const { refresh, whoAmI } = useWhoAmI();
  const { refresh: refreshRealms } = useRealms();
  const { addAlert, addError } = useAlerts();
  const [realm, setRealm] = useState<RealmRepresentation>();
  const [rawTextResourceFile, setRawTextResourceFile] = useState("");

  const form = useForm<RealmRepresentation>({
    mode: "onChange",
  });

  const { handleSubmit, formState } = form;

  const handleFileChange = (obj?: object, rawText?: string) => {
    const defaultRealm = { id: "", realm: "", enabled: true };
    const cleanedRawText = rawText?.replace(/^{|}$/g, "").trim() || "";

    setRawTextResourceFile(cleanedRawText);
    setRealm(convertFormValuesToObject(obj || defaultRealm));
  };

  const save = async (fields: RealmRepresentation) => {
    try {
      if (realm && !Object.keys(realm).length && rawTextResourceFile.length) {
        addAlert(t("invalidResourceFile"), AlertVariant.danger);
        return;
      }

      await adminClient.realms.create({
        ...realm,
        ...convertFormValuesToObject(fields),
        id: fields.realm,
      });
      addAlert(t("saveRealmSuccess"));

      refresh();
      await refreshRealms();
      navigate(toDashboard({ realm: fields.realm }));
    } catch (error) {
      addError("saveRealmError", error);
    }
  };

  return (
    <>
      <ViewHeader titleKey="createRealm" subKey="realmExplain" />
      <PageSection variant="light">
        <FormProvider {...form}>
          <FormAccess
            isHorizontal
            onSubmit={handleSubmit(save)}
            role="view-realm"
            isReadOnly={!whoAmI.canCreateRealm()}
          >
            <JsonFileUpload
              id="kc-realm-filename"
              allowEditingUploadedText
              onChange={handleFileChange}
            />
            <TextControl
              name="realm"
              label={t("realmNameField")}
              rules={{ required: t("required") }}
            />
            <DefaultSwitchControl
              name="enabled"
              label={t("enabled")}
              defaultValue={true}
            />
            <ActionGroup>
              <FormSubmitButton
                formState={formState}
                allowInvalid
                allowNonDirty
              >
                {t("create")}
              </FormSubmitButton>
              <Button variant="link" onClick={() => navigate(-1)}>
                {t("cancel")}
              </Button>
            </ActionGroup>
          </FormAccess>
        </FormProvider>
      </PageSection>
    </>
  );
}
