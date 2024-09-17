import type { DownloadedUsersRepresentation } from '../../defs/custom/userRepresentation.js';
import type { UsersResponseRepresentation } from '../../defs/custom/userRepresentation.js';
import type { KeycloakAdminClient } from '../../client.js';
import Resource from '../resource.js';
import { CustomAdminRealm } from './adminRealm.js';

export type CustomUserQuery = Partial<{
	searchRealm: string;
	search: string;
	searchUser: string;
	searchToms: string;
	searchPhone: string;
	first: number;
	max: number;
	sortAsc: string;
	sortField: string;
}>;

export class CustomUsers extends Resource<{ realm?: string }> {
	private customAdminRealm: CustomAdminRealm;

	public delete;

	public findUsers = this.makeRequest<CustomUserQuery, UsersResponseRepresentation>({
		method: 'GET',
		path: '/users-info/search',
		queryParamKeys: [
			'searchRealm',
			'search',
			'searchUser',
			'searchToms',
			'searchPhone',
			'first',
			'max',
			'sortAsc',
			'sortField',
		],
	});

	public sendLogin = this.makeUpdateRequest<
		unknown,
		string[], // userIds
		void
	>({
		method: 'POST',
		path: '/users-toms/send/login',
	});

	public sendLoginAndResetPassword = this.makeUpdateRequest<
		unknown,
		string[], // userIds
		void
	>({
		method: 'POST',
		path: '/users-toms/credential/reset-with-send-login',
	});

	public downloadCSVTemplate = this.makeRequest<unknown, ArrayBuffer>({
		method: 'POST',
		path: '/users-toms/downloadImportUsersTemplate/csv',
		headers: {
			accept: 'application/octet-stream',
		},
	});

	public downloadExcelTemplate = this.makeRequest<unknown, ArrayBuffer>({
		method: 'POST',
		path: '/users-toms/downloadImportUsersTemplate/xlsx',
		headers: {
			accept: 'application/octet-stream',
		},
	});

	public importFile = (filename?: string) =>
		this.makeUpdateRequest<unknown, FormData>({
			method: 'POST',
			path: '/users-toms/uploadUsers',
			headers: {
				'Content-Disposition': `form-data; name="file"; filename=${filename}`,
			},
		});

	public downloadUsers = this.makeUpdateRequest<
		unknown,
		DownloadedUsersRepresentation,
		ArrayBuffer
	>({
		method: 'POST',
		path: '/users-toms/downloadUsers',
	});

	public resetPassword = this.makeUpdateRequest<
		unknown,
		string[], // userIds
		void
	>({
		method: 'POST',
		path: '/manage/credential/reset',
	});

	public block = this.makeUpdateRequest<
		unknown,
		string[], // userIds
		void
	>({
		method: 'POST',
		path: '/manage/block',
	});

	public unlock = this.makeUpdateRequest<
		unknown,
		string[], // userIds
		void
	>({
		method: 'POST',
		path: '/manage/unlock',
	});

	public impersonation = this.makeUpdateRequest<
		{ id: string },
		{ user: string; realm: string },
		Record<string, any>
	>({
		method: 'POST',
		path: '/users-toms/impersonation/{id}',
		urlParamKeys: ["id"],
	});

	constructor(client: KeycloakAdminClient) {
		super(client, {
			path: '/realms/{realm}',
			getUrlParams: () => ({
				realm: client.realmName,
			}),
			getBaseUrl: () => client.baseUrl,
		});

		this.customAdminRealm = new CustomAdminRealm(client);

		this.delete = this.customAdminRealm.makeRequest<{ id: string }, void>({
			method: 'DELETE',
			path: '/users/{id}',
			urlParamKeys: ['id'],
		});
	}
}
