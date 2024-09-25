import AuthenticationFlowRepresentation from "@keycloak/keycloak-admin-client/lib/defs/authenticationFlowRepresentation";
import type { OptionType } from "@keycloak/keycloak-ui-shared/dist/controls/select-control/SelectControl";
import { ActionGroup, Button } from "@patternfly/react-core";
import { sortBy } from "lodash-es";
import { useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { SelectControl } from "@keycloak/keycloak-ui-shared";
import { useAdminClient } from "../../admin-client";
import { FormAccess } from "../../components/form/FormAccess";
import { useFetch } from "../../utils/useFetch";
import {useCustomConfig} from "../../customLogic/context/CustomConfigContext";

type AuthenticationOverridesProps = {
  save: () => void;
  reset: () => void;
  protocol?: string;
  hasConfigureAccess?: boolean;
};

export const AuthenticationOverrides = ({
  protocol,
  save,
  reset,
  hasConfigureAccess,
}: AuthenticationOverridesProps) => {
  const { adminClient } = useAdminClient();
  const openIdConnect = "openid-connect";
  const { isCustomTheme } = useCustomConfig();

  const { t } = useTranslation();
  const [flows, setFlows] = useState<AuthenticationFlowRepresentation[]>([]);

  useFetch(
    () => adminClient.authenticationManagement.getFlows(),
    (flows) => {
      let filteredFlows = [
        ...flows.filter((flow) => flow.providerId !== "client-flow"),
      ];
      filteredFlows = sortBy(filteredFlows, [(f) => f.alias]);
      setFlows(filteredFlows);
    },
    [],
  );

  const flowOptions = useMemo<OptionType>(() => {
    return [
      { key: "", value: t("choose") },
      ...flows.map(({ id, alias }) => ({ key: id!, value: alias! })),
    ]
  }, [flows]);

  return (
    <FormAccess
      role="manage-clients"
      fineGrainedAccess={hasConfigureAccess}
      isHorizontal
    >
      <SelectControl
        name="authenticationFlowBindingOverrides.browser"
        label={t("browserFlow")}
        labelIcon={t("browserFlowHelp")}
        controller={{
          defaultValue: "",
        }}
        options={flowOptions}
      />
      {protocol === openIdConnect && (
        <SelectControl
          name="authenticationFlowBindingOverrides.direct_grant"
          label={t("directGrant")}
          labelIcon={t("directGrantHelp")}
          controller={{
            defaultValue: "",
          }}
          options={flowOptions}
        />
      )}
      {isCustomTheme && protocol === openIdConnect && (
        <SelectControl
          name="authenticationFlowBindingOverrides.reset_credential"
          label={t("restResetCredentialsFlow")}
          labelIcon={t("restResetCredentialsFlowHelp")}
          controller={{
            defaultValue: "",
          }}
          options={flowOptions}
        />
      )}
      {isCustomTheme && protocol === openIdConnect && (
        <SelectControl
          name="authenticationFlowBindingOverrides.registration"
          label={t("restRegistrationFlow")}
          labelIcon={t("restRegistrationFlowHelp")}
          controller={{
            defaultValue: "",
          }}
          options={flowOptions}
        />
      )}
      <ActionGroup>
        <Button
          variant="secondary"
          onClick={save}
          data-testid="OIDCAuthFlowOverrideSave"
        >
          {t("save")}
        </Button>
        <Button
          variant="link"
          onClick={reset}
          data-testid="OIDCAuthFlowOverrideRevert"
        >
          {t("revert")}
        </Button>
      </ActionGroup>
    </FormAccess>
  );
};
