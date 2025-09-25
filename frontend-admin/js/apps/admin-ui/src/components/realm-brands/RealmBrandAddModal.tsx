import {
  Button,
  ButtonVariant,
  Modal,
  ModalVariant,
} from "@patternfly/react-core";
import { useTranslation } from "react-i18next";
import { BrandRepresentation } from "@keycloak/keycloak-admin-client/lib/defs/brandRepresentation";

type RealmBrandAddModalProps = {
  brands: BrandRepresentation[];
  onAdd?: (brandId: string) => void;
  onClose: () => void;
};

export const RealmBrandAddModal = ({
  onAdd,
  onClose,
}: RealmBrandAddModalProps) => {
  const { t } = useTranslation();

  const handleAddBrand = () => {
    // добавление будет пофикшено в отдельном коммите
    onAdd?.("550e8400-e29b-41d4-a716-446655440003");
  };

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
          onClick={handleAddBrand}
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
      {"Add brand"}
    </Modal>
  );
};
