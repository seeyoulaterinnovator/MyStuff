import {AlertVariant, MenuToggle, Select, SelectList, SelectOption,} from "@patternfly/react-core";
import {useState} from "react";
import {useTranslation} from "react-i18next";
import {UserPostRoleRepresentation} from "@keycloak/keycloak-admin-client/lib/defs/custom/userPostRoleRepresentation";
import {useAdminClient} from "../../admin-client";
import {useAlerts} from "@keycloak/keycloak-ui-shared";
import {useRealm} from "../../context/realm-context/RealmContext";

type ChangeTypeDropdownProps = {
  userPostId: string;
  defaultUserPostRoleId: number;
  userPostRoles: UserPostRoleRepresentation[];
  onChange: () => void;
  isReadonly?: boolean;
};

export const UserPostRoleDropdown = ({
                                       userPostId,
                                       defaultUserPostRoleId,
                                       userPostRoles,
                                       onChange,
                                       isReadonly
                                     }: ChangeTypeDropdownProps) => {
  const {adminClient} = useAdminClient();
  const {realm} = useRealm();
  const {t} = useTranslation();
  const [open, setOpen] = useState(false);
  const {addAlert, addError} = useAlerts();
  const [isChanging, setIsChanging] = useState(false);
  const [userPostRoleId, setUserPostRoleId] = useState(defaultUserPostRoleId);

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
          {userPostRoles.find(role => role.id === userPostRoleId)?.name
            || t("changeUserPostRoleTo")}
        </MenuToggle>
      )}
      selected={userPostRoleId}
      defaultValue={defaultUserPostRoleId}
      onSelect={async (_, value) => {
        setIsChanging(true);
        try {
          await adminClient.userPosts.updateUserPostRole({
            realm
          }, {
            id: userPostId,
            roleId: value as number
          });
          setOpen(false);
          addAlert(t("changeUserPostRoleSuccess", {userPostId}), AlertVariant.success);
          onChange?.();
        } catch (error) {
          addError(t("changeUserPostRoleError", {userPostId, error}));
          setUserPostRoleId(defaultUserPostRoleId);
        } finally {
          setIsChanging(false);
        }
      }}
    >
      <SelectList>
        {userPostRoles.map((role) => (
          <SelectOption key={role.id} value={role.id}>
            {role.name}
          </SelectOption>
        ))}
      </SelectList>
    </Select>
  );
};
