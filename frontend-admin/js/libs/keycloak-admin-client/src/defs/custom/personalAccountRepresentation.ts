import type { Response } from "../jsonResponse.js";

export type PersonalAccountRepresentation = {
  uuid: string;
  value: string;
};

export type PersonalAccountPostRepresentation = {
  post_id: string;
  accounts: PersonalAccountRepresentation[];
};

export type PersonalAccountPostResultRepresentation = {
  data: PersonalAccountPostRepresentation;
};

export interface PersonalAccountPostResponseRepresentation
  extends Response<PersonalAccountPostResultRepresentation> {}

export type PersonalAccountValuesRepresentation = string[];

export type PersonalAccountIdsRepresentation = string[];

export type PersonalAccountsResultRepresentation = {
  accounts: PersonalAccountRepresentation[];
};

export interface PersonalAccountsResponseRepresentation
  extends Response<PersonalAccountsResultRepresentation> {}
