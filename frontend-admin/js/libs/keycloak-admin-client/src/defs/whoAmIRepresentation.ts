export type AccessChecker = {
  hasAll: (accessOrTypes: (readonly AccessType[]) | AccessType, ...types: AccessType[]) => boolean;
  hasAny: (accessOrTypes: (readonly AccessType[]) | AccessType, ...types: AccessType[]) => boolean;
};
export type AccessTypeFunc = (accessChecker: AccessChecker) => boolean;

export type CustomAccessType =
  | "hide-manage-buttons" 
  | "button-download-template-csv" 
  | "button-download-template-xlsx"
  | "button-import-file-csv"
  | "button-export-csv"
  | "button-export-xlsx"
  | "button-reset-password"
  | "button-block-users"
  | "button-unlock-users"
  | "button-add-user"
  | "button-required-actions"
  | "button-delete-customer"
  | "button-add-customer"
  | "edit-attributes"
  | "edit-sessions"
  | "edit-role-mappings"
  | "edit-consents"
  | "edit-credentials"
  | "hide-select-all"
  | "hide-user-search"
  | "edit-federated-identity"
  | "edit-customer"
  | "edit-groups"
  | "edit-details"
  | "create-realm"
  | "custom-query-users"
  | "custom-query-groups"
  | "custom-query-clients"
  | "custom-view-realm"
  | "custom-view-clients"
  | "custom-view-users"
  | "custom-view-events"
  | "custom-view-identity-providers"
  | "custom-view-authorization"
  | "manage-bss"
  | "impersonation";

export type AccessType =
  | "view-realm"
  | "view-identity-providers"
  | "manage-identity-providers"
  | "impersonation"
  | "create-client"
  | "manage-users"
  | "query-realms"
  | "view-authorization"
  | "query-clients"
  | "query-users"
  | "manage-events"
  | "manage-realm"
  | "view-events"
  | "view-users"
  | "view-clients"
  | "manage-authorization"
  | "manage-clients"
  | "query-groups"
  | "anyone"
  | CustomAccessType
  | AccessTypeFunc;

export default interface WhoAmIRepresentation {
  userId: string;
  realm: string;
  displayName: string;
  locale: string;
  createRealm: boolean;
  realm_access: { [key: string]: AccessType[] };
}
