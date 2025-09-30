import {
  Button,
  ButtonVariant,
  Form,
  Modal,
  ModalVariant,
} from "@patternfly/react-core";
import { useTranslation } from "react-i18next";
import { BrandRepresentation } from "@keycloak/keycloak-admin-client/lib/defs/brandRepresentation";
import { FormProvider, SubmitHandler, UseFormReturn } from "react-hook-form";
import { SelectControl } from "@keycloak/keycloak-ui-shared";

export type RealmBrandAddForm = {
  brandId: string;
};

type RealmBrandAddModalProps = {
  brands: BrandRepresentation[];
  form: UseFormReturn<RealmBrandAddForm>;
  save: SubmitHandler<RealmBrandAddForm>;
  onClose: () => void;
};

export const RealmBrandAddModal = ({
  brands,
  form,
  save,
  onClose,
}: RealmBrandAddModalProps) => {
  const { t } = useTranslation();

  return (
    <Modal
      variant={ModalVariant.small}
      title={t("addBrand")}
      isOpen
      onClose={onClose}
      actions={[
        <Button
          data-testid="add-realm-brand-confirm-button"
          key="confirm"
          variant="primary"
          type="submit"
          form="realm-brand-add-form"
        >
          {t("add")}
        </Button>,
        <Button
          id="modal-cancel"
          data-testid="cancel"
          key="cancel"
          variant={ButtonVariant.link}
          onClick={onClose}
        >
          {t("cancel")}
        </Button>,
      ]}
    >
      <Form id="realm-brand-add-form" onSubmit={form.handleSubmit(save)}>
        <FormProvider {...form}>
          <SelectControl
            name="brandId"
            label={t("brand")}
            controller={{
              defaultValue: "",
              rules: {
                required: t("required"),
              },
            }}
            options={brands.map((brand) => ({
              key: brand.brandId,
              value: brand.brandName,
            }))}
          />
        </FormProvider>
      </Form>
    </Modal>
  );
};
