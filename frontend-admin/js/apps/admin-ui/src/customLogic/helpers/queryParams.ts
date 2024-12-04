import { capitalizeText } from "./transforms";
import type { QueryParam } from "../constants/queryParams";
import { Literal } from "../constants/literal";
import type { MapQueryParams, TupleQueryParam } from "../types/queryParams";

export class URLQueryParams {
  static readonly get = (
    checkedParams: QueryParam[],
    params: URLSearchParams,
  ) => {
    const result: MapQueryParams = {};

    checkedParams.map((item) => {
      if (typeof item !== "function") {
        const accessInCapitalizeCamel = capitalizeText(item);
        result[`${Literal.PARAM}${accessInCapitalizeCamel}`] =
          params.get(item) || undefined;
      }
    });

    return result;
  };

  static readonly update = (options: TupleQueryParam[]) => {
    const [baseUrl, hash] = window.location.href.split("#");
    const hashUrl = new URL(hash, window.location.origin);

    options.forEach(([nameParam, valueParam]) => {
      if (valueParam) {
        hashUrl.searchParams.set(nameParam, valueParam);
      } else {
        hashUrl.searchParams.delete(nameParam);
      }
    });

    window.history.replaceState(
      null,
      "",
      `${baseUrl}#${hashUrl.pathname}${hashUrl.search}`,
    );
  };
}
