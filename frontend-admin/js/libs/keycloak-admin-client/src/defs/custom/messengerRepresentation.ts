import type { Response } from "../jsonResponse.js";

export interface MessengerRepresentation {
  id: string;
  label: string;
  name: string;
}

export interface MessengersRepresentation {
  messengers: MessengerRepresentation[];
}

export type MessengersResponseRepresentation =
  Response<MessengersRepresentation>;
