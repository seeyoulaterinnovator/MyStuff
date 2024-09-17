import { label } from "@keycloak/keycloak-ui-shared";
import {
  Divider,
  Dropdown,
  DropdownGroup,
  DropdownItem,
  DropdownList,
  MenuToggle,
  SearchInput,
  Spinner,
  Split,
  SplitItem,
  Stack,
  StackItem,
} from "@patternfly/react-core";
import { CheckIcon } from "@patternfly/react-icons";
import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useRealms } from "../../../context/RealmsContext";
import { useRecentRealms } from "../../../context/RecentRealms";

import "./realm-simple-selector.css";

type RealmTextProps = {
  displayName?: string;
  name?: string;
  isSelected?: boolean;
};

const RealmText = ({ name, displayName, isSelected }: RealmTextProps) => {
  const { t } = useTranslation();

  return (
    <Split className="keycloak__realm_selector__list-item-split">
      <SplitItem isFilled>
        <Stack>
          {displayName ? (
            <StackItem className="pf-v5-u-font-weight-bold" isFilled>
              {label(t, displayName)}
            </StackItem>
          ) : null}
          <StackItem isFilled>{name}</StackItem>
        </Stack>
      </SplitItem>
      <SplitItem>{isSelected && <CheckIcon />}</SplitItem>
    </Split>
  );
};

interface RealmSimpleSelectorProps {
  id: string;
  value?: string;
  onChange?: (realmName: string) => void;
}

// * Based on frontend-admin\js\apps\admin-ui\src\components\realm-selector
export const RealmSimpleSelector = ({
  id,
  value,
  onChange,
}: RealmSimpleSelectorProps) => {
  const { realms } = useRealms();
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState("");
  const { t } = useTranslation();
  const recentRealms = useRecentRealms();
  const [currentRealmName, setCurrentRealmName] = useState(value);

  useEffect(() => {
    setCurrentRealmName(value);
  }, [value]);

  const selectedRealm = useMemo(() => {
    return realms.find((item) => item.name === currentRealmName);
  }, [realms, currentRealmName]);

  const all = useMemo(
    () =>
      realms
        .map((realm) => {
          const used = recentRealms.some((name) => name === realm.name);
          return { realm, used };
        })
        .sort((r1, r2) => {
          if (r1.used == r2.used) return 0;
          if (r1.used) return -1;
          if (r2.used) return 1;
          return 0;
        }),
    [recentRealms, realms],
  );

  const filteredItems = useMemo(() => {
    const normalizedSearch = search.trim().toLowerCase();

    if (normalizedSearch.length === 0) {
      return all;
    }

    return search.trim() === ""
      ? all
      : all.filter(
          (r) =>
            r.realm.name.toLowerCase().includes(normalizedSearch) ||
            label(t, r.realm.displayName)
              ?.toLowerCase()
              .includes(normalizedSearch),
        );
  }, [search, all]);

  return (
    <Dropdown
      id={id}
      className="keycloak__realm_selector__dropdown"
      isOpen={open}
      toggle={(ref) => (
        <MenuToggle
          ref={ref}
          data-testid="realmSelector"
          onClick={() => {
            setOpen(!open);
          }}
          isFullWidth
        >
          {label(t, selectedRealm?.displayName, currentRealmName)}
        </MenuToggle>
      )}
    >
      <DropdownList>
        {realms.length > 5 && (
          <>
            <DropdownGroup>
              <DropdownList>
                <SearchInput
                  value={search}
                  onChange={(_, value) => setSearch(value)}
                  onClear={() => setSearch("")}
                />
              </DropdownList>
            </DropdownGroup>
            <Divider component="li" />
          </>
        )}
        {realms.length !== 0
          ? filteredItems.map((i) => (
              <DropdownItem
                key={i.realm.name}
                onClick={() => {
                  setCurrentRealmName(i.realm.name);
                  onChange?.(i.realm.name);
                  setOpen(false);
                }}
              >
                <RealmText
                  {...i.realm}
                  isSelected={currentRealmName === i.realm.name}
                />
              </DropdownItem>
            ))
          : [
              <DropdownItem key="loader">
                <Spinner size="sm" /> {t("loadingRealms")}
              </DropdownItem>,
            ]}
      </DropdownList>
    </Dropdown>
  );
};
