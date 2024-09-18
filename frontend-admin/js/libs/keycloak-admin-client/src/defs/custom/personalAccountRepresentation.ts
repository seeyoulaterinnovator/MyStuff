import type {Response} from "../jsonResponse.js";

export type PersonalAccountRepresentation = {
  uuid: string;

  value: string;
};

export type PersonalAccountPostRepresentation = {
  post_id: string;

  accounts: PersonalAccountRepresentation[];
};

export type PersonalAccountPostsResponseRepresentationData = {
  data: PersonalAccountPostRepresentation;
}

export interface PersonalAccountPostsResponseRepresentation
  extends Response<PersonalAccountPostsResponseRepresentationData> {}
