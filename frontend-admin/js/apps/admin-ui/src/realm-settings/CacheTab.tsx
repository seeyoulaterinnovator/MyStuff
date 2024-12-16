import type RealmRepresentation from "@keycloak/keycloak-admin-client/lib/defs/realmRepresentation";
import {
  AlertVariant,
  Button,
  FormGroup,
  PageSection,
} from "@patternfly/react-core";
import { useTranslation } from "react-i18next";
import { FormAccess } from "../components/form/FormAccess";
import { useState } from "react";
import { useAlerts } from "../components/alert/Alerts";
import { useAdminClient } from "../admin-client";
import { HelpItem } from "@keycloak/keycloak-ui-shared";
import { useWhoAmI } from "../context/whoami/WhoAmI";
import { RealmName } from "@keycloak/keycloak-admin-client/lib/defs/custom/realmTypes";

const CacheRow = ({
  label,
  help,
  isDisabled,
  clear,
}: {
  label: string;
  help: string;
  isDisabled: boolean;
  clear: () => Promise<any>;
}) => {
  const { addAlert, addError } = useAlerts();
  const { t } = useTranslation();
  const [isLoading, setIsLoading] = useState(false);
  return (
    <FormGroup
      label={t(label)}
      fieldId={`kc-cache-${label}`}
      labelIcon={
        <HelpItem helpText={t(help)} fieldLabelId={`kc-cache-${label}`} />
      }
    >
      <Button
        type="button"
        isDisabled={!isLoading && isDisabled}
        isLoading={isLoading}
        onClick={async () => {
          setIsLoading(true);
          try {
            await clear();
            addAlert(t("cacheClearSuccess"), AlertVariant.success);
          } catch (error) {
            addError(t("cacheClearError"), error);
          } finally {
            setIsLoading(false);
          }
        }}
      >
        {t("clear")}
      </Button>
    </FormGroup>
  );
};

export const RealmSettingsCacheTab = ({
  realm,
}: {
  realm: RealmRepresentation;
}) => {
  const [isDisabled, setIsDisabled] = useState(false);
  const { adminClient } = useAdminClient();
  const { whoAmI } = useWhoAmI();
  const isMasterAuthRealm = whoAmI.getRealm() === RealmName.MASTER;
  return (
    <PageSection variant="light">
      <FormAccess isHorizontal role="manage-realm" className="pf-v5-u-mt-lg">
        <CacheRow
          label="realmCache"
          help="realmCacheClearHelp"
          isDisabled={isDisabled}
          clear={() => {
            setIsDisabled(true);
            try {
              return adminClient.cache.clearRealmCache(realm);
            } finally {
              setIsDisabled(false);
            }
          }}
        />
        <CacheRow
          label="userCache"
          help="userCacheClearHelp"
          isDisabled={isDisabled}
          clear={() => {
            setIsDisabled(true);
            try {
              return adminClient.cache.clearUserCache(realm);
            } finally {
              setIsDisabled(false);
            }
          }}
        />
        <CacheRow
          label="keysCache"
          help="keysCacheClearHelp"
          isDisabled={isDisabled || !isMasterAuthRealm}
          clear={() => {
            setIsDisabled(true);
            try {
              return adminClient.cache.clearKeysCache(realm);
            } finally {
              setIsDisabled(false);
            }
          }}
        />
        <CacheRow
          label="customerCache"
          help="customerCacheHelp"
          isDisabled={isDisabled || !isMasterAuthRealm}
          clear={() => {
            setIsDisabled(true);
            try {
              return adminClient.customCache.clearCustomerCache({
                realm: RealmName.MASTER,
              });
            } finally {
              setIsDisabled(false);
            }
          }}
        />
        <CacheRow
          label="userPostCache"
          help="userPostCacheHelp"
          isDisabled={isDisabled}
          clear={() => {
            setIsDisabled(true);
            try {
              return adminClient.customCache.clearUserPostCache({
                realm: RealmName.MASTER,
              });
            } finally {
              setIsDisabled(false);
            }
          }}
        />
      </FormAccess>
    </PageSection>
  );
};
