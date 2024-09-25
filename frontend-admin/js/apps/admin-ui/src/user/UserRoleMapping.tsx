import type { RoleMappingPayload } from "@keycloak/keycloak-admin-client/lib/defs/roleRepresentation";
import { AlertVariant } from "@patternfly/react-core";
import { useTranslation } from "react-i18next";
import { useAdminClient } from "../admin-client";
import { useAlerts } from "../components/alert/Alerts";
import { RoleMapping, Row } from "../components/role-mapping/RoleMapping";
import { useRealm } from "../context/realm-context/RealmContext";

type UserRoleMappingProps = {
  id: string;
  name: string;
};

export const UserRoleMapping = ({ id, name }: UserRoleMappingProps) => {
  const { adminClient } = useAdminClient();
  const { realm, searchRealm } = useRealm();

  const { t } = useTranslation();
  const { addAlert, addError } = useAlerts();

  const assignRoles = async (rows: Row[]) => {
    try {
      const realmRoles = rows
        .filter((row) => row.client === undefined)
        .map((row) => row.role as RoleMappingPayload)
        .flat();
      if(realm != searchRealm) {
        await adminClient.customUsers.addRealmRoleMappings({
          id,
          realm: searchRealm,
          roles: realmRoles,
        });
      } else {
        await adminClient.users.addRealmRoleMappings({
          id,
          roles: realmRoles,
        });
      }
      await Promise.all(
        rows
          .filter((row) => row.client !== undefined)
          .map((row) => {
            if(realm !== searchRealm) {
              return adminClient.customUsers.addClientRoleMappings({
                id,
                clientUniqueId: row.client!.id!,
                realm: searchRealm,
                roles: [row.role as RoleMappingPayload]
              });
            } else {
              return adminClient.users.addClientRoleMappings({
                id,
                clientUniqueId: row.client!.id!,
                roles: [row.role as RoleMappingPayload]
              });
            }
          }),
      );
      addAlert(t("userRoleMappingUpdatedSuccess"), AlertVariant.success);
    } catch (error) {
      addError("roleMappingUpdatedError", error);
    }
  };

  return <RoleMapping name={name} id={id} type="users" save={assignRoles} />;
};
