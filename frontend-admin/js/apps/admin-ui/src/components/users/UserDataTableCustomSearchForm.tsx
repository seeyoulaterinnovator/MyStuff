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
    <Form onSubmit={handleSubmit(searchUserWithCustomFilters)}>
      <ToolbarGroup className="pf-m-wrap" variant="filter-group">
        <ToolbarItem>
          <TextInput
            id="value"
            style={{ maxWidth: "150px" }}
            placeholder={t("search")}
            {...register("search")}
          />
        </ToolbarItem>
        <ToolbarItem>
          <TextInput
            id="userId"
            style={{ maxWidth: "150px" }}
            placeholder={t("searchByUserId")}
            {...register("searchUser")}
          />
        </ToolbarItem>
        <ToolbarItem>
          <TextInput
            id="tomsId"
            style={{ maxWidth: "150px" }}
            placeholder={t("searchByTomsId")}
            {...register("searchToms")}
          />
        </ToolbarItem>
        <ToolbarItem>
          <TextInput
            id="phone"
            style={{ maxWidth: "150px" }}
            placeholder={t("searchByPhone")}
            {...register("searchPhone")}
          />
        </ToolbarItem>

        <ToolbarItem>
          <Button
            style={{ maxWidth: "150px" }}
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
    </Form>
  );
}
