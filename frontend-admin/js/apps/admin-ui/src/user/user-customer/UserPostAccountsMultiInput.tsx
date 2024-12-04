import { useTranslation } from "react-i18next";
import { useState } from "react";
import {
  AlertVariant,
  Button,
  Chip,
  ChipGroup,
  Flex,
  FlexItem,
  TextInput,
} from "@patternfly/react-core";
import { PlusCircleIcon } from "@patternfly/react-icons";
import { PersonalAccountRepresentation } from "@keycloak/keycloak-admin-client/lib/defs/custom/personalAccountRepresentation";

import "../user-customer.css";
import { useRealm } from "../../context/realm-context/RealmContext";
import { useAlerts } from "../../components/alert/Alerts";
import { useAdminClient } from "../../admin-client";

type UserPostAccountsMultiInputProps = {
  postId: string;
  defaultAccounts: PersonalAccountRepresentation[];
  isReadonly?: boolean;
};

export const UserPostAccountsMultiInput = (
  props: UserPostAccountsMultiInputProps,
) => {
  const { postId, defaultAccounts, isReadonly } = props;

  const { adminClient } = useAdminClient();
  const { t } = useTranslation();
  const { realm } = useRealm();
  const { addAlert, addError } = useAlerts();
  const [accounts, setAccounts] = useState(defaultAccounts);
  const [value, setValue] = useState<string>();
  const [isAdding, setIsAdding] = useState(false);
  const [isChanging, setIsChanging] = useState(false);

  return (
    <Flex direction={{ default: "column" }}>
      {(!isAdding || accounts.length > 0) && (
        <ChipGroup numChips={Number.MAX_SAFE_INTEGER}>
          {accounts!.map((account) => (
            <Chip
              key={account.uuid}
              className="kc-user-customer-chip"
              onClick={async (event) => {
                event.stopPropagation();
                setIsChanging(true);
                try {
                  await adminClient.personalAccounts.deletePersonalAccounts(
                    { realm, postId },
                    [account.uuid],
                  );
                  addAlert(
                    t("deleteUserPostAccountSuccess", { postId }),
                    AlertVariant.success,
                  );
                  setAccounts(
                    accounts.filter(({ uuid }) => account.uuid !== uuid),
                  );
                } catch (error) {
                  addError(t("deleteUserPostAccountError", { postId }), error);
                } finally {
                  setIsChanging(false);
                }
              }}
              isReadOnly={isChanging}
            >
              {account.value}
            </Chip>
          ))}
          {!isReadonly && !isAdding && (
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
      {!isReadonly && isAdding && (
        <Flex
          gap={{ default: "gap" }}
          className="kc-user-customer-account-cell"
        >
          <FlexItem
            grow={{ default: "grow" }}
            spacer={{ default: "spacerNone" }}
          >
            <TextInput
              aria-label="Account value"
              value={value}
              onChange={(_event, value) => {
                setValue(value);
              }}
              isDisabled={isChanging}
            />
          </FlexItem>
          <FlexItem>
            <Button
              type="button"
              isDisabled={!value?.trim() || isChanging}
              isLoading={isChanging}
              onClick={async () => {
                if (!value) return;
                setIsChanging(true);
                try {
                  const newAccounts = (
                    await adminClient.personalAccounts.createPersonalAccounts(
                      { realm, postId },
                      [value],
                    )
                  ).results.accounts;
                  addAlert(
                    t("addUserPostAccountSuccess", { postId }),
                    AlertVariant.success,
                  );
                  setAccounts(accounts.concat(newAccounts));
                  setValue("");
                } catch (error) {
                  addError(t("addUserPostAccountError", { postId }), error);
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
