import type { CaseType, ConvertToCase } from "../types/caseStyle";

export const convertCase = <
  T extends string,
  From extends CaseType,
  To extends CaseType,
>(
  str: T,
  from: From,
  to: To,
): ConvertToCase<T, From, To> => {
  const transformers = {
    toCamel: (input: string) =>
      input.replace(/[-_](.)/g, (_, char) =>
        char.toUpperCase(),
      ) as ConvertToCase<T, From, To>,
    fromCamel: (input: string, separator: string) =>
      input
        .replace(/([a-z])([A-Z])/g, `$1${separator}$2`)
        .toLowerCase() as ConvertToCase<T, From, To>,
    toKebab: (input: string) =>
      input.replace(/_/g, "-") as ConvertToCase<T, From, To>,
    toSnake: (input: string) =>
      input.replace(/-/g, "_") as ConvertToCase<T, From, To>,
  };

  switch (true) {
    case from === "camel" && to === "kebab":
      return transformers.fromCamel(str, "-");

    case from === "camel" && to === "snake":
      return transformers.fromCamel(str, "_");

    case from === "kebab" && to === "camel":
      return transformers.toCamel(str);

    case from === "snake" && to === "camel":
      return transformers.toCamel(str);

    case from === "kebab" && to === "snake":
      return transformers.toSnake(str);

    case from === "snake" && to === "kebab":
      return transformers.toKebab(str);

    default:
      return str as ConvertToCase<T, From, To>;
  }
};

export const capitalizeText = <T extends string>(text: T): Capitalize<T> => {
  return `${text.charAt(0).toUpperCase()}${text.slice(1)}` as Capitalize<T>;
};

export const addBomAndConvertToBlob: (text: ArrayBuffer) => Blob = (text) => {
  return new Blob([new Uint8Array([0xef, 0xbb, 0xbf]), text], {
    type: "text/plain;charset=utf-8",
  });
};
