import type RealmRepresentation from "@keycloak/keycloak-admin-client/lib/defs/realmRepresentation";
import type { UserProfileConfig } from "@keycloak/keycloak-admin-client/lib/defs/userProfileMetadata";
import type { CustomUserQuery } from "@keycloak/keycloak-admin-client/lib/resources/custom/users";
import { RealmName } from "@keycloak/keycloak-admin-client/lib/defs/custom/realmTypes";
import {
  Button,
  ButtonVariant,
  SearchInput,
  ToolbarItem,
  Dropdown,
  MenuToggle,
  DropdownList,
  DropdownItem,
  ToolbarGroup,
  DropdownGroup,
  Divider,
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
import { CustomUserToolbarAction } from "../../customLogic/constants/user";
import { UploadButton } from "../../customLogic/ui/UploadButton";
import { CustomUsersAction } from "../../customLogic/types/users";
import { useWhoAmI } from "../../context/whoami/WhoAmI";
import { useCustomConfig } from "../../customLogic/context/CustomConfigContext";

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
  customFilters: CustomUserQuery;
  searchUserWithCustomFilters: (customFilters: CustomUserQuery) => void;
  onCustomAction?: (action: CustomUsersAction) => void;
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
  customFilters,
  searchUserWithCustomFilters,
  onCustomAction,
}: UserDataTableToolbarItemsProps) {
  const { t } = useTranslation();
  const [kebabOpen, setKebabOpen] = useState(false);
  const [isActionsOpen, setIsActionsOpen] = useState(false);

  const { hasAccess, getAccesses } = useAccess();
  const { whoAmI } = useWhoAmI();
  const { isCustomTheme } = useCustomConfig();
  const isMasterAuthRealm = whoAmI.getRealm() === RealmName.MASTER;
  const isMasterSearchRealm = customFilters.searchRealm === RealmName.MASTER;
  const isManagerSearchRealm = customFilters.searchRealm === RealmName.MANAGER;

  // Only needs query-users access to attempt add/delete of users.
  // This is because the user could have fine-grained access to users
  // of a group.  There is no way to know this without searching the
  // permissions of every group.
  const isManager = hasAccess("query-users");

  const {
    withCustomViewRealmAccess,
    withViewUsersAccess,
    withManageUsersAccess,
    withManageRealmAccess,
    withHideManageButtonsAccess,
    // withButtonDownloadTemplateCsvAccess,
    // withButtonDownloadTemplateXlsxAccess,
    withButtonImportFileCsvAccess,
    withButtonExportCsvAccess,
    withButtonExportXlsxAccess,
    withButtonResetPasswordAccess,
    withButtonBlockUsersAccess,
    withButtonUnlockUsersAccess,
    withButtonAddUserAccess,
    withCreateRealmAccess,
  } = getAccesses([
    "custom-view-realm",
    "view-users",
    "manage-users",
    "manage-realm",
    "hide-manage-buttons",
    // 'button-download-template-csv',
    // 'button-download-template-xlsx',
    "button-import-file-csv",
    "button-export-csv",
    "button-export-xlsx",
    "button-reset-password",
    "button-block-users",
    "button-unlock-users",
    "button-add-user",
    "create-realm",
  ]);
  const withShowManageButtonAndManageUsersAccess =
    !withHideManageButtonsAccess && withManageUsersAccess;

  const searchItem = () => {
    return (
      <ToolbarGroup
        className="pf-m-wrap pf-v5-u-w-100"
        variant="filter-group"
        {...(isCustomTheme && searchType === "custom"
          ? {
              spaceItems: { default: "spaceItemsSm" },
            }
          : {})}
      >
        <ToolbarItem>
          <SearchDropdown
            searchType={searchType}
            onSelect={(searchType) => {
              clearAllFilters();
              setSearchType(searchType);
            }}
          />
        </ToolbarItem>
        {searchType === "default" && defaultSearchInput()}
        {searchType === "attribute" && attributeSearchInput()}
        {searchType === "custom" && customSearchInput()}
      </ToolbarGroup>
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
        searchUserWithCustomFilters={(newCustomFilters) => {
          searchUserWithCustomFilters(newCustomFilters);
          setSearchDropdownOpen(false);
        }}
      />
    );
  };

  const bruteForceProtectionToolbarItem = !realm.bruteForceProtected ? (
    ((isCustomTheme &&
      withCreateRealmAccess &&
      withManageRealmAccess &&
      withManageUsersAccess) ||
      !isCustomTheme) && (
      <ToolbarItem>
        <Button
          variant={!isCustomTheme ? ButtonVariant.link : undefined}
          onClick={toggleDeleteDialog}
          data-testid="delete-user-btn"
          isDisabled={hasSelectedRows}
        >
          {t("deleteUser")}
        </Button>
      </ToolbarItem>
    )
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
          {((isCustomTheme &&
            withCreateRealmAccess &&
            withManageRealmAccess &&
            withManageUsersAccess) ||
            !isCustomTheme) && (
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
          )}
          {((isCustomTheme &&
            withShowManageButtonAndManageUsersAccess &&
            withButtonUnlockUsersAccess) ||
            isMasterAuthRealm ||
            !isCustomTheme) && (
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
          )}
        </DropdownList>
      </Dropdown>
    </ToolbarItem>
  );

  const actionItems = (
    <ToolbarGroup
      align={{ md: "alignLeft", "2xl": "alignRight" }}
      className="pf-m-wrap"
    >
      {/* {isCustomTheme &&
          withCustomViewRealmAccess &&
          (withButtonDownloadTemplateCsvAccess || isMasterAuthRealm) && (
            <ToolbarItem>
              <Button
                onClick={() =>
                  onCustomAction?.({
                    type: CustomUserToolbarAction.DOWNLOAD_TEMPLATE_CSV,
                  })
                }
              >
                {t("downloadTemplateCSV")}
              </Button>
            </ToolbarItem>
          )} */}
      {/* {isCustomTheme &&
          withCustomViewRealmAccess &&
          (withButtonDownloadTemplateXlsxAccess || isMasterAuthRealm) && (
            <ToolbarItem>
              <Button
                onClick={() =>
                  onCustomAction?.({
                    type: CustomUserToolbarAction.DOWNLOAD_TEMPLATE_EXCEL,
                  })
                }
              >
                {t("downloadTemplateExcel")}
              </Button>
            </ToolbarItem>
          )} */}
      {isCustomTheme &&
        withShowManageButtonAndManageUsersAccess &&
        withCustomViewRealmAccess &&
        (withButtonImportFileCsvAccess || isMasterAuthRealm) && (
          <ToolbarItem>
            <UploadButton
              extensions={".csv,.xls,.xlsx,.ctl"}
              onUpload={(event) =>
                onCustomAction?.({
                  type: CustomUserToolbarAction.IMPORT_FILE,
                  payload: event.target.files?.[0],
                })
              }
            >
              {t("importFile")}
            </UploadButton>
          </ToolbarItem>
        )}
      {((isCustomTheme &&
        withShowManageButtonAndManageUsersAccess &&
        (withButtonAddUserAccess || isMasterAuthRealm)) ||
        !isCustomTheme) && (
        <ToolbarItem>
          <Button data-testid="add-user" onClick={goToCreate}>
            {t("addUser")}
          </Button>
        </ToolbarItem>
      )}
      {bruteForceProtectionToolbarItem}
      <ToolbarItem>
        <Dropdown
          popperProps={{
            position: "right",
          }}
          toggle={(ref) => (
            <MenuToggle
              ref={ref}
              onClick={() =>
                setIsActionsOpen((prevIsActionsOpen) => !prevIsActionsOpen)
              }
              data-testid="action-dropdown"
            >
              {t("action")}
            </MenuToggle>
          )}
          isOpen={isActionsOpen}
          onOpenChange={setIsActionsOpen}
        >
          <DropdownGroup>
            {isCustomTheme &&
              withViewUsersAccess &&
              !(isMasterSearchRealm || isManagerSearchRealm) && (
                <DropdownList>
                  <DropdownItem
                    onClick={() =>
                      onCustomAction?.({
                        type: CustomUserToolbarAction.SEND_LOGIN,
                      })
                    }
                  >
                    {t("sendLogin")}
                  </DropdownItem>
                  <DropdownItem
                    onClick={() =>
                      onCustomAction?.({
                        type: CustomUserToolbarAction.SEND_LOGIN_AND_RESET_PASSWORD,
                      })
                    }
                  >
                    {t("sendLoginAndResetPassword")}
                  </DropdownItem>
                </DropdownList>
              )}
          </DropdownGroup>
          <Divider component="li" />
          <DropdownGroup>
            <DropdownList>
              {isCustomTheme &&
                withShowManageButtonAndManageUsersAccess &&
                (withButtonBlockUsersAccess || isMasterAuthRealm) && (
                  <DropdownItem
                    onClick={() =>
                      onCustomAction?.({
                        type: CustomUserToolbarAction.BLOCK_USERS,
                      })
                    }
                  >
                    {t("blockUsers")}
                  </DropdownItem>
                )}
              {isCustomTheme &&
                withShowManageButtonAndManageUsersAccess &&
                (withButtonUnlockUsersAccess || isMasterAuthRealm) && (
                  <DropdownItem
                    onClick={() =>
                      onCustomAction?.({
                        type: CustomUserToolbarAction.UNLOCK_USERS,
                      })
                    }
                  >
                    {t("unlockUsers")}
                  </DropdownItem>
                )}
              {isCustomTheme &&
                withShowManageButtonAndManageUsersAccess &&
                (withButtonResetPasswordAccess || isMasterAuthRealm) && (
                  <DropdownItem
                    onClick={() =>
                      onCustomAction?.({
                        type: CustomUserToolbarAction.RESET_PASSWORD,
                      })
                    }
                  >
                    {t("resetPassword")}
                  </DropdownItem>
                )}
            </DropdownList>
          </DropdownGroup>
          <Divider component="li" />
          <DropdownGroup>
            <DropdownList>
              {isCustomTheme &&
                withShowManageButtonAndManageUsersAccess &&
                (withButtonExportCsvAccess || isMasterAuthRealm) && (
                  <DropdownItem
                    onClick={() =>
                      onCustomAction?.({
                        type: CustomUserToolbarAction.EXPORT_CSV,
                      })
                    }
                  >
                    {t("exportCSV")}
                  </DropdownItem>
                )}
              {isCustomTheme &&
                withShowManageButtonAndManageUsersAccess &&
                (withButtonExportXlsxAccess || isMasterAuthRealm) && (
                  <DropdownItem
                    onClick={() =>
                      onCustomAction?.({
                        type: CustomUserToolbarAction.EXPORT_EXCEL,
                      })
                    }
                  >
                    {t("exportExcel")}
                  </DropdownItem>
                )}
            </DropdownList>
          </DropdownGroup>
        </Dropdown>
      </ToolbarItem>
    </ToolbarGroup>
  );

  return (
    <>
      {searchItem()}
      {isManager ? actionItems : null}
    </>
  );
}
