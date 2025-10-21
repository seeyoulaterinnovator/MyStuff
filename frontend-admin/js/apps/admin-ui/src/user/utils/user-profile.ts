import {
  UserProfileAttributeMetadata,
  UserProfileMetadata,
} from "@keycloak/keycloak-admin-client/lib/defs/userProfileMetadata";
import { RealmBrandRepresentation } from "@keycloak/keycloak-admin-client/lib/defs/brandRepresentation";

export function isRequiredAttribute({
  required,
  validators,
}: UserProfileAttributeMetadata): boolean {
  // Check if required is true or if the validators include a validation that would make the attribute implicitly required.
  return required || hasRequiredValidators(validators);
}

/**
 * Checks whether the given validators include a validation that would make the attribute implicitly required.
 */
function hasRequiredValidators(
  validators?: UserProfileAttributeMetadata["validators"],
): boolean {
  // If we don't have any validators, the attribute is not required.
  if (!validators) {
    return false;
  }

  // If the 'length' validator is defined and has a minimal length greater than zero the attribute is implicitly required.
  // We have to do a lot of defensive coding here, because we don't have type information for the validators.
  if (
    "length" in validators &&
    "min" in validators.length &&
    typeof validators.length.min === "number"
  ) {
    return validators.length.min > 0;
  }

  return false;
}

/**
 * Обновляет поле markBrandId в userProfileMetadata
 */
export function updateMarkBrandIdField(
  metadata: UserProfileMetadata | undefined,
  realmBrands: RealmBrandRepresentation[],
): UserProfileMetadata | undefined {
  if (!metadata) return metadata;

  const attributes = [...(metadata.attributes ?? [])];
  const markBrandIndex = attributes.findIndex(
    (attribute) => attribute.name === "markBrandId",
  );
  if (markBrandIndex === -1) return metadata;

  const markBrand = { ...attributes[markBrandIndex] };

  markBrand.annotations = {
    ...(markBrand.annotations ?? {}),
    inputType: "select",
    inputOptionLabels: realmBrands.reduce(
      (acc, brand) => {
        acc[brand.brandId] = brand.brandName;
        return acc;
      },
      {} as Record<string, string>,
    ),
    defaultValue: realmBrands.find((brand) => brand.default)?.brandId,
  };

  markBrand.validators = {
    ...(markBrand.validators ?? {}),
    options: {
      options: realmBrands.map((brand) => brand.brandId),
    },
  };

  attributes[markBrandIndex] = markBrand;

  return { ...metadata, attributes };
}
