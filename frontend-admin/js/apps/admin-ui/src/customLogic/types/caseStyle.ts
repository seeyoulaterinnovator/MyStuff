export type CaseType = "kebab" | "camel" | "snake";

export type ConvertToCase<
  T extends string,
  From extends CaseType,
  To extends CaseType,
> = From extends "camel"
  ? To extends "kebab"
    ? CamelToKebab<T>
    : To extends "snake"
      ? CamelToSnake<T>
      : T
  : From extends "kebab"
    ? To extends "camel"
      ? KebabToCamel<T>
      : To extends "snake"
        ? KebabToSnake<T>
        : T
    : From extends "snake"
      ? To extends "camel"
        ? SnakeToCamel<T>
        : To extends "kebab"
          ? SnakeToKebab<T>
          : T
      : T;

export type CamelToKebab<T extends string> =
  T extends `${infer P1}${infer P2}${infer P3}`
    ? P2 extends Uppercase<P2>
      ? `${Lowercase<P1>}-${Lowercase<P2>}${CamelToKebab<P3>}`
      : `${Lowercase<P1>}${CamelToKebab<`${P2}${P3}`>}`
    : T;

export type CamelToSnake<T extends string> =
  T extends `${infer P1}${infer P2}${infer P3}`
    ? P2 extends Uppercase<P2>
      ? `${Lowercase<P1>}_${Lowercase<P2>}${CamelToSnake<P3>}`
      : `${Lowercase<P1>}${CamelToSnake<`${P2}${P3}`>}`
    : T;

export type KebabToCamel<T extends string> =
  T extends `${infer P1}-${infer P2}${infer P3}`
    ? `${Lowercase<P1>}${Uppercase<P2>}${KebabToCamel<P3>}`
    : T;

export type KebabToSnake<T extends string> = T extends `${infer P1}-${infer P2}`
  ? `${Lowercase<P1>}_${KebabToSnake<P2>}`
  : T;

export type SnakeToCamel<T extends string> =
  T extends `${infer P1}_${infer P2}${infer P3}`
    ? `${Lowercase<P1>}${Uppercase<P2>}${SnakeToCamel<P3>}`
    : T;

export type SnakeToKebab<T extends string> = T extends `${infer P1}_${infer P2}`
  ? `${Lowercase<P1>}-${SnakeToKebab<P2>}`
  : T;
