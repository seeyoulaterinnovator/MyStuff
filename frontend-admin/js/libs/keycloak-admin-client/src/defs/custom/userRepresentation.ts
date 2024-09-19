import type { RealmName } from "./realmTypes.js";
import type {
  ConditionalResponseStatus,
  Response,
} from "../../defs/jsonResponse.js";
import type PageInfoRepresentation from "../custom/pageInfoRepresentation.js";

export enum UserRoleName {
  LPR = "LPR",
  BUH = "BUH",
  TECH = "TECH",
  GUEST = "GUEST",
}

export const DEFAULT_USER_ROLE_ID = 1;

export enum SystemRoleName {
  ACCESS_GRANTED = "access_granted",
}

export enum ExternalSystemName {
  LKB2B = "lkb2b",
  DMP_KC_SIT = "dmp-kc-sit",
  FORPOST = "forpost",
  ITGLOBAL = "itglobal",
  OATS = "oats",
  WIFI = "wifi",
  APP_B2B = "app_b2b",
  B2B = "b2b",
}

export interface UserRoleRepresentation {
  id: number;
  name: UserRoleName;
  description: string;
}

export interface SystemRoleRepresentation {
  id: number;
  name: SystemRoleName;
  externalSystem: {
    id: number;
    name: ExternalSystemName;
    label: string;
    realmId: RealmName;
  };
}

export type ArrayTimeRepresentation = [
  number,
  number,
  number,
  number,
  number,
  number,
];

export interface UserPostRepresentation {
  id: string;
  userId: string;
  tomsId: string;
  dmpId: string;
  userRole: UserRoleRepresentation;
  systemRoles?: SystemRoleRepresentation[];
  selected: boolean;
  organization: string;
  updateTime: ArrayTimeRepresentation;
}

export interface UserInfoRepresentation {
  id: string;
  username: string;
  firstName: string;
  email: string;
  emailVerified: boolean;
  phone: string;
  enabled: boolean;
  userPosts: UserPostRepresentation[];
}

export interface UsersRepresentation {
  "page-info": PageInfoRepresentation;
  "users-info": UserInfoRepresentation[];
}

export type UsersResponseRepresentation = Response<UsersRepresentation>;

export type DownloadedUsersType = "xlsx" | "csv";

export type UserParameters =
  | "USER_ID"
  | "FIRST_NAME"
  | "EMAIL"
  | "PHONE"
  | "TOMS_ID"
  | "DMP_ID"
  | "ROLE"
  | "SYSTEM"
  | "ENABLED";

export type DownloadedUsersRepresentation = {
  type: DownloadedUsersType;
  userIds: string[];
  userParameters: UserParameters[];
};

export enum UserReportStatus {
  DONE = "DONE",
  AWAITING = "AWAITING",
}

export interface ImportUsersReportRepresentation {
  countClones: number;
  countCreatedUsers: number;
  countImportUsers: number;
  id: string;
  importDate: string;
  name: string;
  realmId: RealmName;
  status: UserReportStatus;
}

export interface ImportUsersReportResultRepresentation {
  importUsersReports: ImportUsersReportRepresentation[];
}

export type ImportUsersReportResponseRepresentation =
  Response<ImportUsersReportResultRepresentation>;

export type ActiveImportUsersResponseRepresentation = ConditionalResponseStatus;

export type UserPostsResultRepresentation = {
  user_post: UserPostRepresentation[];
};

export interface UserPostsResponseRepresentation
  extends Response<UserPostsResultRepresentation> {}

export type UserPostSystemRoleRepresentation = {
  systemRoleId: number;
  userPostId: string;
};
