import type {Response} from "../jsonResponse.js";

export type UserPostRoleRepresentation = {
  id: number;

  name: string;

  description: string;
};

export type UserPostRolesResponse = {
  roles: UserPostRoleRepresentation[];
}

export interface UserPostRolesResponseRepresentation
  extends Response<UserPostRolesResponse> {
}

export type UserPostRolesEditRepresentation = {
  id: string;

  roleId: number;
}
