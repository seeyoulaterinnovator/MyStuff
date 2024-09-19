import {
  AlertVariant,
  MenuToggle,
  Select,
  SelectList,
  SelectOption,
} from "@patternfly/react-core";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { UserPostRoleRepresentation } from "js/libs/keycloak-admin-client/src/defs/custom/userPostRepresentation";
import { useAdminClient } from "../../admin-client";
import { useAlerts } from "../../components/alert/Alerts";
import { useRealm } from "../../context/realm-context/RealmContext";

type UserPostRoleDropdownProps = {
  postId?: string;
  defaultRoleId?: number;
  roles: UserPostRoleRepresentation[];
  isReadonly?: boolean;
  onChange?: (role: UserPostRoleRepresentation) => void;
};

export const UserPostRoleDropdown = (props: UserPostRoleDropdownProps) => {
  const { postId, defaultRoleId, roles, isReadonly, onChange } = props;

  const { adminClient } = useAdminClient();
  const { realm } = useRealm();
  const { t } = useTranslation();
  const [open, setOpen] = useState(false);
  const { addAlert, addError } = useAlerts();
  const [isChanging, setIsChanging] = useState(false);
  const [roleId, setRoleId] = useState(defaultRoleId);

  return (
    <Select
      isOpen={open}
      toggle={(ref) => (
        <MenuToggle
          ref={ref}
          onClick={() => setOpen(!open)}
          isExpanded={open}
          isDisabled={isReadonly || isChanging}
        >
          {roles.find((role) => role.id === roleId)?.name || t("changeRoleTo")}
        </MenuToggle>
      )}
      selected={roleId}
      defaultValue={defaultRoleId}
      onSelect={async (_, value) => {
        const role = roles.find((r) => r.id === value);
        if (!role) return;
        if (postId) {
          setIsChanging(true);
          try {
            await adminClient.userPosts.updateUserPostRole(
              {
                realm,
              },
              {
                id: postId,
                roleId: value as number,
              },
            );
            setRoleId(value as number);
            setOpen(false);
            addAlert(
              t("changeUserPostRoleSuccess", { postId }),
              AlertVariant.success,
            );
            onChange?.(role);
          } catch (error) {
            addError(t("changeUserPostRoleError", { postId }), error);
            setRoleId(defaultRoleId);
          } finally {
            setIsChanging(false);
          }
        } else {
          setRoleId(value as number);
          setOpen(false);
          onChange?.(role);
        }
      }}
    >
      <SelectList>
        {roles.map((role) => (
          <SelectOption key={role.id} value={role.id}>
            {role.name}
          </SelectOption>
        ))}
      </SelectList>
    </Select>
  );
};
