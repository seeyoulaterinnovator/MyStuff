import type { CustomUserQuery } from "@keycloak/keycloak-admin-client/lib/resources/custom/users";
import {
  Button,
  TextInput,
  ToolbarGroup,
  ToolbarItem,
} from "@patternfly/react-core";
import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { Form } from "react-router-dom";
import { RealmSimpleSelector } from "../custom/realm-simple-selector/RealmSimpleSelector";
import { useAccess } from "../../context/access/Access";
import { useCustomConfig } from "../../customLogic/context/CustomConfigContext";

const defaultCustomSearchForm: CustomUserQuery = {};

type UserDataTableCustomSearchFormProps = {
  searchUserWithCustomFilters: (newFilters: CustomUserQuery) => void;
  customFilters: CustomUserQuery;
};

export function UserDataTableCustomSearchForm({
  searchUserWithCustomFilters,
  customFilters,
}: UserDataTableCustomSearchFormProps) {
  const { t } = useTranslation();
  const { hasAccess } = useAccess();
  const withHideUserSearchAccess = hasAccess("hide-user-search");
  const { isCustomTheme } = useCustomConfig();

  const { register, reset, handleSubmit, watch, setValue, getValues } =
    useForm<CustomUserQuery>({
      mode: "onChange",
      defaultValues: defaultCustomSearchForm,
    });

  const clearCustomFilters = () => {
    const actualFilters = getValues();
    handleSubmit(() =>
      searchUserWithCustomFilters({
        searchRealm: actualFilters.searchRealm,
      }),
    )();
  };

  useEffect(() => {
    reset(customFilters);
  }, [customFilters]);

  return (
    <Form
      className="pf-v5-u-flex-grow-1"
      onSubmit={handleSubmit(searchUserWithCustomFilters)}
    >
      {isCustomTheme && !withHideUserSearchAccess && (
        <ToolbarGroup
          className="pf-m-wrap pf-m-space-items"
          variant="filter-group"
          spaceItems={{ default: "spaceItemsSm" }}
        >
          <ToolbarItem className="pf-v5-u-flex-grow-1">
            <TextInput
              id="value"
              placeholder={t("search")}
              {...register("search")}
            />
          </ToolbarItem>
          <ToolbarItem className="pf-v5-u-flex-grow-1">
            <TextInput
              id="userId"
              placeholder={t("searchByUserId")}
              {...register("searchUser")}
            />
          </ToolbarItem>
          <ToolbarItem className="pf-v5-u-flex-grow-1">
            <TextInput
              id="tomsId"
              placeholder={t("searchByTomsId")}
              {...register("searchToms")}
            />
          </ToolbarItem>
          <ToolbarItem className="pf-v5-u-flex-grow-1">
            <TextInput
              id="phone"
              placeholder={t("searchByPhone")}
              {...register("searchPhone")}
            />
          </ToolbarItem>
          <ToolbarItem>
            <Button
              data-testid="search-user-attribute-btn"
              variant="primary"
              type="submit"
            >
              {t("search")}
            </Button>
          </ToolbarItem>
          <ToolbarItem>
            <Button variant="primary" onClick={clearCustomFilters}>
              {t("viewAllUsers")}
            </Button>
          </ToolbarItem>
          <ToolbarItem>
            <RealmSimpleSelector
              id="searchRealm"
              {...register("searchRealm")}
              value={watch("searchRealm")}
              realmsSource="accessible"
              onChange={(newSearchRealm) => {
                setValue("searchRealm", newSearchRealm);
                handleSubmit(() =>
                  searchUserWithCustomFilters({
                    searchRealm: newSearchRealm,
                  }),
                )();
              }}
            />
          </ToolbarItem>
        </ToolbarGroup>
      )}
    </Form>
  );
}
