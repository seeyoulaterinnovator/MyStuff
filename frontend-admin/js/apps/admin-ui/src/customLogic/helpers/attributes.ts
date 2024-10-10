import { DataAttribute, HtmlAttributePrefix } from "../constants/attributes";

export const getAttributeName = (
  dataAttribute: DataAttribute,
  prefix: HtmlAttributePrefix = HtmlAttributePrefix.Data,
) => {
  return `${prefix}${dataAttribute}`;
};
