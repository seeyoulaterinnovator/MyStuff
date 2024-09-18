import {useTranslation} from "react-i18next";
import {useState} from "react";
import {AlertVariant, Button, Chip, ChipGroup, Flex, FlexItem, TextInput} from "@patternfly/react-core";
import {PlusCircleIcon} from "@patternfly/react-icons";
import {
  PersonalAccountRepresentation
} from "@keycloak/keycloak-admin-client/lib/defs/custom/personalAccountRepresentation";

import "./user-post-accounts-multi-input.css";
import {useRealm} from "../../context/realm-context/RealmContext";
import {useAlerts} from "@keycloak/keycloak-ui-shared";

type UserPostAccountsMultiInputProps = {
  userPostId: string;
  accounts: PersonalAccountRepresentation[];
  onChange: () => void;
  isReadonly?: boolean;
}

export const UserPostAccountsMultiInput = ({
                                             userPostId,
                                             accounts,
                                             onChange,
                                             isReadonly
                                           }: UserPostAccountsMultiInputProps) => {
  const {t} = useTranslation();
  const {realm} = useRealm();
  const {addAlert, addError} = useAlerts();
  const [value, setValue] = useState<string>();
  const [isAdding, setIsAdding] = useState(false);
  const [isChanging, setIsChanging] = useState(false);

  return (
    <Flex direction={{default: "column"}}>
      <ChipGroup numChips={Number.MAX_SAFE_INTEGER}>
        {accounts!.map((account) => (
          <Chip
            key={account.uuid}
            className="kc-user-customer-account-chip"
            onClick={() => {
              setIsChanging(true);
              try {
                // TODO
                addAlert(t("deleteUserPostAccountSuccess", {userPostId}), AlertVariant.success);
                onChange?.();
              } catch (error) {
                addError(t("deleteUserPostAccountError", {userPostId, error}));
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
          <Button variant="link" icon={<PlusCircleIcon/>} onClick={() => setIsAdding(true)}>
            {t("add")}
          </Button>
        )}
      </ChipGroup>
      {!isReadonly && isAdding && (
        <Flex gap={{default: "gap"}} className="kc-user-customer-account-cell">
          <FlexItem
            grow={{default: "grow"}}
            spacer={{default: "spacerNone"}}
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
              disabled={!value?.trim() || isChanging}
              onClick={() => {
                setIsChanging(true);
                try {
                  // TODO
                  addAlert(t("addUserPostAccountSuccess", {userPostId}), AlertVariant.success);
                  onChange?.();
                } catch (error) {
                  addError(t("addUserPostAccountError", {userPostId, error}));
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
