import {
  ExternalSystemName,
  SystemRoleRepresentation,
} from "@keycloak/keycloak-admin-client/lib/defs/custom/userRepresentation";
import { useTranslation } from "react-i18next";
import { useEffect, useMemo, useState } from "react";
import { KeycloakSelect } from "@keycloak/keycloak-ui-shared";
import { useAdminClient } from "../../admin-client";
import { useRealm } from "../../context/realm-context/RealmContext";
import {
  AlertVariant,
  Button,
  Chip,
  ChipGroup,
  Flex,
  FlexItem,
  SelectOption,
} from "@patternfly/react-core";
import { PlusCircleIcon } from "@patternfly/react-icons";
import "../user-customer.css";
import { useAlerts } from "../../components/alert/Alerts";

type UserPostSystemRoleMultiSelectProps = {
  postId: string;
  allSystemRoles: SystemRoleRepresentation[];
  defaultSystemRoles?: SystemRoleRepresentation[];
  isReadonly?: boolean;
};

const roleToSystem = (role: SystemRoleRepresentation) =>
  role.externalSystem.name;

const findSystemId = (
  roles: SystemRoleRepresentation[],
  system: ExternalSystemName | null,
) =>
  system
    ? roles.find((role) => roleToSystem(role) === system)?.id ?? null
    : null;

export const UserPostSystemRoleMultiSelect = (
  props: UserPostSystemRoleMultiSelectProps,
) => {
  const { postId, allSystemRoles, defaultSystemRoles, isReadonly } = props;

  const { adminClient } = useAdminClient();
  const { t } = useTranslation();
  const { realm } = useRealm();
  const { addAlert, addError } = useAlerts();
  const [system, setSystem] = useState<ExternalSystemName | null>(null);
  const [systems, setSystems] = useState<ExternalSystemName[]>(
    defaultSystemRoles?.map(roleToSystem) || [],
  );
  const [isAdding, setIsAdding] = useState(false);
  const [isChanging, setIsChanging] = useState(false);
  const [isSystemOpen, setIsSystemOpen] = useState(false);

  const allowableSystems = useMemo(() => {
    return allSystemRoles
      .map(roleToSystem)
      .filter((value) => !systems.includes(value));
  }, [allSystemRoles, systems]);

  const systemId = useMemo(
    () => findSystemId(allSystemRoles, system),
    [allSystemRoles, system],
  );

  useEffect(() => {
    setSystem(allowableSystems[0] || null);
  }, [allowableSystems]);

  return (
    <Flex direction={{ default: "column" }}>
      {(!isAdding || systems.length > 0) && (
        <ChipGroup numChips={Number.MAX_SAFE_INTEGER}>
          {systems!.map((system) => {
            const systemId = findSystemId(allSystemRoles, system);
            if (systemId === null) return "";
            return (
              <Chip
                key={system}
                className="kc-user-customer-chip"
                onClick={async (event) => {
                  event.stopPropagation();
                  setIsChanging(true);
                  try {
                    await adminClient.userPosts.deleteUserPostSystemRole(
                      { realm },
                      {
                        userPostId: postId,
                        systemRoleId: systemId,
                      },
                    );
                    addAlert(
                      t("deleteUserPostSystemSuccess", { postId, system }),
                      AlertVariant.success,
                    );
                    setSystems(systems.filter((it) => it !== system));
                  } catch (error) {
                    addError(
                      t("deleteUserPostSystemError", {
                        postId,
                        system,
                      }),
                      error,
                    );
                  } finally {
                    setIsChanging(false);
                  }
                }}
                isReadOnly={isChanging || isReadonly}
              >
                {system}
              </Chip>
            );
          })}
          {!isReadonly && !isAdding && allowableSystems.length > 0 && (
            <Button
              variant="link"
              icon={<PlusCircleIcon />}
              onClick={() => setIsAdding(true)}
            >
              {t("add")}
            </Button>
          )}
        </ChipGroup>
      )}
      {!isReadonly && isAdding && allowableSystems.length > 0 && (
        <Flex
          gap={{ default: "gap" }}
          className="kc-user-customer-account-cell"
        >
          <FlexItem>
            <KeycloakSelect
              toggleId="system"
              onToggle={setIsSystemOpen}
              onSelect={(value) => {
                setSystem(value.toString() as ExternalSystemName);
                setIsSystemOpen(false);
              }}
              selections={system || undefined}
              aria-label={t("system")}
              isOpen={isSystemOpen}
            >
              {allowableSystems.map((allowableSystem) => (
                <SelectOption
                  selected={allowableSystem === system}
                  key={allowableSystem}
                  value={allowableSystem}
                >
                  {allowableSystem}
                </SelectOption>
              ))}
            </KeycloakSelect>
          </FlexItem>
          <FlexItem>
            <Button
              type="button"
              isDisabled={!system || isChanging}
              isLoading={isChanging}
              onClick={async () => {
                if (!system || systemId == null) return;
                setIsChanging(true);
                try {
                  await adminClient.userPosts.createUserPostSystemRole(
                    { realm },
                    {
                      userPostId: postId,
                      systemRoleId: systemId,
                    },
                  );
                  addAlert(
                    t("addUserPostSystemSuccess", { postId, system }),
                    AlertVariant.success,
                  );
                  setSystems(systems.concat(system));
                  setSystem(null);
                } catch (error) {
                  addError(
                    t("addUserPostSystemError", { postId, system }),
                    error,
                  );
                } finally {
                  setIsChanging(false);
                }
              }}
            >
              {t("add")}
            </Button>
          </FlexItem>
        </Flex>
      )}
    </Flex>
  );
};
