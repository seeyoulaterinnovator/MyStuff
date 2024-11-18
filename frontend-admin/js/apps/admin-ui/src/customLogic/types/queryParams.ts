import type { Literal } from "../constants/literal";
import type { QueryParam } from "../constants/queryParams";
import type { LiteralExpansion } from "./literal";

export type QueryParamKey = LiteralExpansion<
  Capitalize<QueryParam>,
  Literal.PARAM
>;

export type MapQueryParams = Partial<Record<QueryParamKey, string | undefined>>;

export type TupleQueryParam = [QueryParam, string | undefined];
