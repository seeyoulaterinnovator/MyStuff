import type GroupRepresentation from "@keycloak/keycloak-admin-client/lib/defs/groupRepresentation";
import type { UserProfileMetadata } from "@keycloak/keycloak-admin-client/lib/defs/userProfileMetadata";
import {
  isUserProfileError,
  setUserProfileServerError,
} from "@keycloak/keycloak-ui-shared";
import { AlertVariant, PageSection } from "@patternfly/react-core";
import { TFunction } from "i18next";
import { useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAdminClient } from "../admin-client";
import { useAlerts } from "../components/alert/Alerts";
import { KeycloakSpinner } from "../components/keycloak-spinner/KeycloakSpinner";
import { ViewHeader } from "../components/view-header/ViewHeader";
import { useRealm } from "../context/realm-context/RealmContext";
import { useFetch } from "../utils/useFetch";
import { UserForm } from "./UserForm";
import { toUserRepresentation, UserFormFields } from "./form-state";
import { toUser } from "./routes/User";

import "./user-section.css";
import RealmRepresentation from "@keycloak/keycloak-admin-client/lib/defs/realmRepresentation";
import { QueryParam } from "../customLogic/constants/queryParams";
import { useCustomConfig } from "../customLogic/context/CustomConfigContext";

export default function CreateUser() {
  const { adminClient } = useAdminClient();

  const { t } = useTranslation();
  const { addAlert, addError } = useAlerts();
  const navigate = useNavigate();
  const { realm: realmName, realmRepresentation: realm } = useRealm();
  const form = useForm<UserFormFields>({ mode: "onChange" });
  const [addedGroups, setAddedGroups] = useState<GroupRepresentation[]>([]);
  const [userProfileMetadata, setUserProfileMetadata] =
    useState<UserProfileMetadata>();
  const [searchRealm, setSearchRealm] = useState<RealmRepresentation>();

  const [searchParams] = useSearchParams();
  const searchRealmName = useMemo(() => {
    return searchParams.get(QueryParam.SEARCH_REALM) || realmName;
  }, [searchParams, realmName]);
  const { isCustomTheme } = useCustomConfig();

  useFetch(
    () => {
      if (isCustomTheme) {
        return adminClient.realms.findOne({
          realm: realmName,
          searchRealm: searchRealmName,
        });
      } else {
        return Promise.resolve(realm);
      }
    },
    setSearchRealm,
    [searchRealmName, realm],
  );

  useFetch(
    () =>
      adminClient.users.getProfileMetadata({
        realm: realmName,
        searchRealm: searchRealmName,
      }),
    (userProfileMetadata) => {
      if (!userProfileMetadata) {
        throw new Error(t("notFound"));
      }

      form.setValue("attributes.locale", realm?.defaultLocale || "");
      setUserProfileMetadata(userProfileMetadata);
    },
    [searchRealmName],
  );

  const save = async (data: UserFormFields) => {
    try {
      const createdUser = await adminClient.users.create({
        ...toUserRepresentation(data),
        groups: addedGroups.map((group) => group.path!),
        enabled: true,
        searchRealm: searchRealmName,
      });

      addAlert(t("userCreated"), AlertVariant.success);
      navigate(
        toUser({ id: createdUser.id, realm: realmName, tab: "settings" }),
      );
    } catch (error) {
      if (isUserProfileError(error)) {
        setUserProfileServerError(error, form.setError, ((key, param) =>
          t(key as string, param as any)) as TFunction);
      } else {
        addError("userCreateError", error);
      }
    }
  };

  if (!realm || !searchRealm || !userProfileMetadata) {
    return <KeycloakSpinner />;
  }

  return (
    <>
      <ViewHeader
        titleKey={t("createUser")}
        className="kc-username-view-header"
      />
      <PageSection variant="light">
        <UserForm
          form={form}
          realm={realm}
          searchRealm={searchRealm}
          userProfileMetadata={userProfileMetadata}
          onGroupsUpdate={setAddedGroups}
          save={save}
        />
      </PageSection>
    </>
  );
}
