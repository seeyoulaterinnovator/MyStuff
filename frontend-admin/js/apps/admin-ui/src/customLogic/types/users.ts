import type { UIAction } from "./actions";
import type {
  CustomUserToolbarAction,
  CustomImportUsersToolbarAction,
} from "../constants/user";

export type CustomUsersAction =
  | UIAction<
      Exclude<CustomUserToolbarAction, CustomUserToolbarAction.IMPORT_FILE>
    >
  | UIAction<CustomUserToolbarAction.IMPORT_FILE, File | undefined>;

export type CustomImportUsersAction = UIAction<
  CustomImportUsersToolbarAction.IMPORT_FILE,
  File | undefined
>;
