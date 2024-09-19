import type { Response } from "../jsonResponse.js";
import { SystemRoleRepresentation } from "./userRepresentation.js";

export type UserPostRoleRepresentation = {
  id: number;
  name: string;
  description: string;
};

export type UserPostCreateRepresentation = {
  userId: string;
  tomsId: string;
  dmpId: string;
  roleId: number;
};

export type UserPostRolesResultRepresentation = {
  roles: UserPostRoleRepresentation[];
};

export interface UserPostRolesResponseRepresentation
  extends Response<UserPostRolesResultRepresentation> {}

export type UserPostRolesEditRepresentation = {
  id: string;
  roleId: number;
};

export type UserPostSystemRolesResultRepresentation = Record<
  "system-roles",
  SystemRoleRepresentation[]
>;

export interface UserPostSystemRolesResponseRepresentation
  extends Response<UserPostSystemRolesResultRepresentation> {}
