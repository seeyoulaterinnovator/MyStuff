import ClientRepresentation from "../clientRepresentation.js";

export type CustomClientRepresentation = ClientRepresentation & {
  mainRedirectUri?: string;
};
