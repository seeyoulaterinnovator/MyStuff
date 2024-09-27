export type LiteralExpansion<
  Target extends string = string,
  PrefixLiteral extends string = "",
  SuffixLiteral extends string = "",
> = `${PrefixLiteral}${Target}${SuffixLiteral}`;
