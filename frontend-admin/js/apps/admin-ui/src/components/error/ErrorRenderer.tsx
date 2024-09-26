import {
  Alert,
  AlertActionCloseButton,
  AlertActionLink,
  AlertVariant,
  PageSection,
} from "@patternfly/react-core";
import { useTranslation } from "react-i18next";
import { useEnvironment } from "@keycloak/keycloak-ui-shared";

import { type FallbackProps } from "../../context/ErrorBoundary";

export const ErrorRenderer = ({ error, withSignOut }: FallbackProps) => {
  const { t } = useTranslation();
  const { keycloak } = useEnvironment();

  const reset = () => {
    window.location.href = window.location.origin + window.location.pathname;
  };

  const signOut = () => {
    keycloak.logout({ redirectUri: "" });
  };

  return (
    <PageSection>
      <Alert
        isInline
        variant={AlertVariant.danger}
        title={error.message}
        actionClose={
          <AlertActionCloseButton title={error.message} onClose={reset} />
        }
        actionLinks={
          <>
            <AlertActionLink onClick={reset}>{t("retry")}</AlertActionLink>
            {withSignOut && (
              <AlertActionLink onClick={signOut}>
                {t("signOut")}
              </AlertActionLink>
            )}
          </>
        }
      ></Alert>
    </PageSection>
  );
};
