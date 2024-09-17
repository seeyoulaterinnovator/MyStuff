import type RealmRepresentation from "@keycloak/keycloak-admin-client/lib/defs/realmRepresentation";
import type { UserProfileConfig } from "@keycloak/keycloak-admin-client/lib/defs/userProfileMetadata";
import type { CustomUserQuery } from "@keycloak/keycloak-admin-client/lib/resources/custom/users";
import {
  Button,
  ButtonVariant,
  SearchInput,
  ToolbarItem,
  InputGroupItem,
  Dropdown,
  MenuToggle,
  DropdownList,
  DropdownItem,
  ToolbarGroup,
} from "@patternfly/react-core";
import { ArrowRightIcon, EllipsisVIcon } from "@patternfly/react-icons";
import { ReactNode, useState } from "react";
import { useTranslation } from "react-i18next";

import { useAccess } from "../../context/access/Access";
import { SearchDropdown, SearchType } from "../../user/details/SearchFilter";
import { UserAttribute } from "./UserDataTable";
import { UserDataTableAttributeSearchForm } from "./UserDataTableAttributeSearchForm";
import DropdownPanel from "../dropdown-panel/DropdownPanel";
import { UserDataTableCustomSearchForm } from "./UserDataTableCustomSearchForm";
import type { RealmNameRepresentation } from "../../context/RealmsContext";
import { CustomUserToolbarAction } from "../../customLogic/constants/user";
import { UploadButton } from "../../customLogic/ui/UploadButton";
import type { CustomUsersActions } from "../../customLogic/types/users";

type UserDataTableToolbarItemsProps = {
  searchDropdownOpen: boolean;
  setSearchDropdownOpen: (open: boolean) => void;
  realm: RealmRepresentation;
  hasSelectedRows: boolean;
  toggleDeleteDialog: () => void;
  toggleUnlockUsersDialog: () => void;
  goToCreate: () => void;
  searchType: SearchType;
  setSearchType: (searchType: SearchType) => void;
  searchUser: string;
  setSearchUser: (searchUser: string) => void;
  activeFilters: UserAttribute[];
  setActiveFilters: (activeFilters: UserAttribute[]) => void;
  refresh: () => void;
  profile: UserProfileConfig;
  clearAllFilters: () => void;
  createAttributeSearchChips: () => ReactNode;
  searchUserWithAttributes: () => void;
  realms: RealmNameRepresentation[];
  customFilters: CustomUserQuery;
  searchUserWithCustomFilters: (customFilters: CustomUserQuery) => void;
  onCustomAction?: (action: CustomUsersActions) => void;
};

export function UserDataTableToolbarItems({
  searchDropdownOpen,
  setSearchDropdownOpen,
  realm,
  hasSelectedRows,
  toggleDeleteDialog,
  toggleUnlockUsersDialog,
  goToCreate,
  searchType,
  setSearchType,
  searchUser,
  setSearchUser,
  activeFilters,
  setActiveFilters,
  refresh,
  profile,
  clearAllFilters,
  createAttributeSearchChips,
  searchUserWithAttributes,
  realms,
  customFilters,
  searchUserWithCustomFilters,
  onCustomAction,
}: UserDataTableToolbarItemsProps) {
  const { t } = useTranslation();
  const [kebabOpen, setKebabOpen] = useState(false);

  const { hasAccess } = useAccess();

  // Only needs query-users access to attempt add/delete of users.
  // This is because the user could have fine-grained access to users
  // of a group.  There is no way to know this without searching the
  // permissions of every group.
  const isManager = hasAccess("query-users");

  const searchItem = () => {
    return (
      <ToolbarItem>
        <ToolbarGroup className="pf-m-wrap" variant="filter-group">
          <InputGroupItem>
            <SearchDropdown
              searchType={searchType}
              onSelect={(searchType) => {
                clearAllFilters();
                setSearchType(searchType);
              }}
            />
          </InputGroupItem>
          {searchType === "default" && defaultSearchInput()}
          {searchType === "attribute" && attributeSearchInput()}
          {searchType === "custom" && customSearchInput()}
        </ToolbarGroup>
      </ToolbarItem>
    );
  };

  const defaultSearchInput = () => {
    return (
      <ToolbarItem>
        <SearchInput
          data-testid="table-search-input"
          placeholder={t("searchForUser")}
          aria-label={t("search")}
          value={searchUser}
          onSearch={(_, _v, attribute) => {
            setSearchUser(attribute["haswords"]);
            refresh();
          }}
          onKeyDown={(e) => {
            if (e.key === "Enter") {
              const target = e.target as HTMLInputElement;
              setSearchUser(target.value);
              refresh();
            }
          }}
          onClear={() => {
            setSearchUser("");
            refresh();
          }}
        />
      </ToolbarItem>
    );
  };

  const attributeSearchInput = () => {
    return (
      <>
        <DropdownPanel
          buttonText={t("selectAttributes")}
          setSearchDropdownOpen={setSearchDropdownOpen}
          searchDropdownOpen={searchDropdownOpen}
          width="15vw"
        >
          <UserDataTableAttributeSearchForm
            activeFilters={activeFilters}
            setActiveFilters={setActiveFilters}
            profile={profile}
            createAttributeSearchChips={createAttributeSearchChips}
            searchUserWithAttributes={() => {
              searchUserWithAttributes();
              setSearchDropdownOpen(false);
            }}
          />
        </DropdownPanel>
        <Button
          icon={<ArrowRightIcon />}
          variant="control"
          onClick={() => {
            searchUserWithAttributes();
            setSearchDropdownOpen(false);
          }}
          aria-label={t("searchAttributes")}
        />
      </>
    );
  };

  const customSearchInput = () => {
    return (
      <UserDataTableCustomSearchForm
        customFilters={customFilters}
        realms={realms}
        searchUserWithCustomFilters={(newCustomFilters) => {
          searchUserWithCustomFilters(newCustomFilters);
          setSearchDropdownOpen(false);
        }}
      />
    );
  };

  const bruteForceProtectionToolbarItem = !realm.bruteForceProtected ? (
    <ToolbarItem>
      <Button
        variant={ButtonVariant.link}
        onClick={toggleDeleteDialog}
        data-testid="delete-user-btn"
        isDisabled={hasSelectedRows}
      >
        {t("deleteUser")}
      </Button>
    </ToolbarItem>
  ) : (
    <ToolbarItem>
      <Dropdown
        toggle={(ref) => (
          <MenuToggle
            ref={ref}
            isExpanded={kebabOpen}
            variant="plain"
            onClick={() => setKebabOpen(!kebabOpen)}
          >
            <EllipsisVIcon />
          </MenuToggle>
        )}
        isOpen={kebabOpen}
        shouldFocusToggleOnSelect
      >
        <DropdownList>
          <DropdownItem
            key="deleteUser"
            component="button"
            isDisabled={hasSelectedRows}
            onClick={() => {
              toggleDeleteDialog();
              setKebabOpen(false);
            }}
          >
            {t("deleteUser")}
          </DropdownItem>

          <DropdownItem
            key="unlock"
            component="button"
            onClick={() => {
              toggleUnlockUsersDialog();
              setKebabOpen(false);
            }}
          >
            {t("unlockAllUsers")}
          </DropdownItem>
        </DropdownList>
      </Dropdown>
    </ToolbarItem>
  );

  const actionItems = (
    <>
      <ToolbarGroup
        align={{
          md: "alignLeft",
          "2xl": "alignRight",
        }}
        className="pf-m-wrap"
      >
        <ToolbarItem>
          <Button
            onClick={() => onCustomAction?.({ type: CustomUserToolbarAction.SEND_LOGIN })}
          >
            {t("sendLogin")}
          </Button>
        </ToolbarItem>
        <ToolbarItem>
          <Button
            onClick={() =>
              onCustomAction?.(
                { type: CustomUserToolbarAction.SEND_LOGIN_AND_RESET_PASSWORD, }
              )
            }
          >
            {t("sendLoginAndResetPassword")}
          </Button>
        </ToolbarItem>
      </ToolbarGroup>
      <ToolbarGroup
        align={{ md: "alignLeft", "2xl": "alignRight" }}
        className="pf-m-wrap"
      >
        {/* <ToolbarItem>
          <Button
            onClick={() =>
              onCustomAction?.({ type: CustomUserToolbarAction.DOWNLOAD_TEMPLATE_CSV })
            }
          >
            {t("downloadTemplateCSV")}
          </Button>
        </ToolbarItem>
        <ToolbarItem>
          <Button
            onClick={() =>
              onCustomAction?.({ type: CustomUserToolbarAction.DOWNLOAD_TEMPLATE_EXCEL })
            }
          >
            {t("downloadTemplateExcel")}
          </Button>
        </ToolbarItem> */}
        <ToolbarItem>
          <UploadButton 
            extensions={".csv,.xls,.xlsx,.ctl"}
            onUpload={(event) => onCustomAction?.({ type: CustomUserToolbarAction.IMPORT_FILE, payload: event.target.files?.[0] })}
          >
            {t("importFile")}
          </UploadButton>
        </ToolbarItem>
        {/* <ToolbarItem>
          <Button
            onClick={() => onCustomAction?.({ type: CustomUserToolbarAction.EXPORT_CSV })}
          >
            {t("exportCSV")}
          </Button>
        </ToolbarItem>
        <ToolbarItem>
          <Button
            onClick={() =>
              onCustomAction?.({ type: CustomUserToolbarAction.EXPORT_EXCEL })
            }
          >
            {t("exportExcel")}
          </Button>
        </ToolbarItem> */}
        <ToolbarItem>
          <Button
            onClick={() =>
              onCustomAction?.({ type: CustomUserToolbarAction.RESET_PASSWORD })
            }
          >
            {t("resetPassword")}
          </Button>
        </ToolbarItem>
        <ToolbarItem>
          <Button
            onClick={() =>
              onCustomAction?.({ type: CustomUserToolbarAction.BLOCK_USERS })
            }
          >
            {t("blockUsers")}
          </Button>
        </ToolbarItem>
        <ToolbarItem>
          <Button
            onClick={() =>
              onCustomAction?.({ type: CustomUserToolbarAction.UNLOCK_USERS })
            }
          >
            {t("unlockUsers")}
          </Button>
        </ToolbarItem>
        <ToolbarItem>
          <Button data-testid="add-user" onClick={goToCreate}>
            {t("addUser")}
          </Button>
        </ToolbarItem>
        {bruteForceProtectionToolbarItem}
      </ToolbarGroup>
    </>
  );

  return (
    <>
      {searchItem()}
      {isManager ? actionItems : null}
    </>
  );
}
