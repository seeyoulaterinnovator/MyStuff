import { useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useAdminClient } from "../admin-client";
import { useFetch } from "../utils/useFetch";
import {
  BrandRepresentation,
  RealmBrandRepresentation,
} from "@keycloak/keycloak-admin-client/lib/defs/brandRepresentation";
import {
  AlertVariant,
  Button,
  ButtonVariant,
  ToolbarItem,
} from "@patternfly/react-core";
import { KeycloakDataTable } from "../components/table-toolbar/KeycloakDataTable";
import { ListEmptyState } from "../components/list-empty-state/ListEmptyState";
import { OkIcon } from "@patternfly/react-icons";
import { useRealm } from "../context/realm-context/RealmContext";
import { useConfirmDialog } from "../components/confirm-dialog/ConfirmDialog";
import { useAlerts } from "../components/alert/Alerts";
import { RealmBrandAddModal } from "../components/realm-brands/RealmBrandAddModal";

export const BrandsTab = () => {
  const { adminClient } = useAdminClient();
  const { t } = useTranslation();
  const { addAlert, addError } = useAlerts();

  const { realm, realmBrands, refresh: refreshRealm } = useRealm();
  const [allBrands, setAllBrands] = useState<BrandRepresentation[]>([]);
  const [isShowRealmBrandAddModal, setIsShowRealmBrandAddModal] =
    useState(false);
  const [selected, setSelected] = useState<RealmBrandRepresentation | null>(
    null,
  );
  const [key, setKey] = useState(0);
  const refresh = () => setKey(key + 1);

  useEffect(() => {
    refresh();
  }, [realmBrands]);

  useFetch(
    async () => {
      const response = await adminClient.customBrands.getAllBrands();
      return response.results.allBrands;
    },
    setAllBrands,
    [realm],
  );

  const filteredBrands = useMemo(() => {
    const realmBrandIds = realmBrands.map((brand) => brand.brandId);
    return allBrands.filter((brand) => !realmBrandIds.includes(brand.brandId));
  }, [allBrands, realmBrands]);

  const loader = async () => {
    return await Promise.resolve(realmBrands);
  };

  const setAsDefault = async (brandId: string) => {
    await adminClient.customBrands.setDefaultBrand({
      realm,
      brandId,
    });
    refreshRealm();
  };

  const addBrand = async (brandId: string) => {
    await adminClient.customBrands.addRealmBrand({
      realm,
      brandId,
    });
    setIsShowRealmBrandAddModal(false);
    refreshRealm();
  };

  const [toggleDeleteDialog, DeleteConfirm] = useConfirmDialog({
    titleKey: "removeRealmBrandTitle",
    messageKey: t("removeRealmBrandConfirm", { name: selected?.brandName }),
    continueButtonLabel: "remove",
    continueButtonVariant: ButtonVariant.danger,
    onCancel: () => {
      setSelected(null);
      refresh();
    },
    onConfirm: async () => {
      try {
        await adminClient.customBrands.removeRealmBrand({
          realm,
          brandId: selected!.brandId,
        });
        addAlert(
          t("realmBrandRemoveSuccess", { name: selected?.brandName }),
          AlertVariant.success,
        );
        refreshRealm();
      } catch (error) {
        addError("realmBrandRemoveError", error);
      } finally {
        setSelected(null);
        refresh();
      }
    },
  });

  return (
    <>
      {isShowRealmBrandAddModal && (
        <RealmBrandAddModal
          brands={filteredBrands}
          onAdd={addBrand}
          onClose={() => setIsShowRealmBrandAddModal(false)}
        />
      )}
      <DeleteConfirm />
      <KeycloakDataTable
        data-testid="realm-brands"
        key={`realm-brands-${key}`}
        loader={loader}
        ariaLabelKey="realmBrandList"
        withoutRefreshButton={true}
        toolbarItem={
          <ToolbarItem>
            <Button
              data-testid="addBrand"
              onClick={() => setIsShowRealmBrandAddModal(true)}
            >
              {t("addBrand")}
            </Button>
          </ToolbarItem>
        }
        actions={[
          {
            title: t("setAsDefaultBrand"),
            onRowClick: async (row) => {
              setAsDefault(row.brandId);
              return false;
            },
          },
          {
            title: t("delete"),
            onRowClick: async (row) => {
              setSelected(row);
              toggleDeleteDialog();
              return false;
            },
          },
        ]}
        columns={[
          {
            name: "brandName",
            displayKey: t("name"),
          },
          {
            name: "default",
            displayKey: t("defaultBrand"),
            cellRenderer: (row) => {
              return row.default ? <OkIcon /> : "";
            },
          },
        ]}
        emptyState={
          <ListEmptyState
            message={t(`noRealmBrands-realm-brands`)}
            instructions={t(`noRealmBrandsInstructions-realm-brands`)}
            primaryActionText={t("addBrand")}
            onPrimaryAction={() => setIsShowRealmBrandAddModal(true)}
          />
        }
      />
    </>
  );
};
