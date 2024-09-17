import type { UIAction } from './actions';
import type { CustomUserToolbarAction } from '../constants/user';

export type CustomUsersActions =
	| UIAction<Exclude<CustomUserToolbarAction, CustomUserToolbarAction.IMPORT_FILE>>
	| UIAction<CustomUserToolbarAction.IMPORT_FILE, File | undefined>;
