import { useTranslation } from "react-i18next";
import { UploadButton } from "../../../customLogic/ui/UploadButton";
import type { CustomImportUsersAction } from "../../../customLogic/types/users";
import { CustomImportUsersToolbarAction } from "../../../customLogic/constants/user";

export interface ImportUsersDataTableToolbarItemsProps {
  onAction: (action: CustomImportUsersAction) => void;
}

export const ImportUsersDataTableToolbarItems = (
  props: ImportUsersDataTableToolbarItemsProps,
) => {
  const { onAction } = props;
  const { t } = useTranslation();

  return (
    <UploadButton
      extensions={".csv,.xls,.xlsx,.ctl"}
      onUpload={(event) => {
        onAction?.({
          type: CustomImportUsersToolbarAction.IMPORT_FILE,
          payload: event.target.files?.[0],
        });
      }}
    >
      {t("uploadImportUsersFile")}
    </UploadButton>
  );
};
