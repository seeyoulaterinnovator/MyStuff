import type {
  AccessType,
  AccessTypeFunc,
} from "@keycloak/keycloak-admin-client/lib/defs/whoAmIRepresentation";
import { PropsWithChildren, useEffect, useState } from "react";
import { useRealm } from "../../context/realm-context/RealmContext";
import { useWhoAmI } from "../../context/whoami/WhoAmI";
import {
  createNamedContext,
  useRequiredContext,
} from "@keycloak/keycloak-ui-shared";
import { isReadonlyArray } from "../../customLogic/helpers/guards";
import { LiteralExpansion } from "../../customLogic/types/literal";
import { KebabToCamel } from "../../customLogic/types/caseStyle";
import {
  capitalizeText,
  convertCase,
} from "../../customLogic/helpers/transforms";
import { Literal } from "../../customLogic/constants/literal";

export type MapAccessKey = LiteralExpansion<
  Capitalize<KebabToCamel<Exclude<AccessType, AccessTypeFunc>>>,
  Literal.WITH,
  Literal.ACCESS
>;
export type MapAccess = Partial<Record<MapAccessKey, boolean>>;

type AccessContextProps = {
  hasAccess: (...types: AccessType[]) => boolean;
  hasSomeAccess: (...types: AccessType[]) => boolean;
  getAccesses: (checkedAccess: AccessType[]) => MapAccess;
};

export const AccessContext = createNamedContext<AccessContextProps | undefined>(
  "AccessContext",
  undefined,
);

export const useAccess = () => useRequiredContext(AccessContext);

export const AccessContextProvider = ({ children }: PropsWithChildren) => {
  const { whoAmI } = useWhoAmI();
  const { realm } = useRealm();
  const [access, setAccess] = useState<readonly AccessType[]>([]);

  useEffect(() => {
    const realmAccess = whoAmI.getRealmAccess()[realm];

    const getExtendedAccess = () => {
      const extendedAccess: AccessType[] = [];
      const { hasAccess: hasBaseAccess } =
        getAccessCheckersForTarget(realmAccess);
      const { hasAccess: hasExtendedAccess } =
        getAccessCheckersForTarget(extendedAccess);

      switch (true) {
        case whoAmI.canCreateRealm():
          extendedAccess.push("create-realm");

        case hasBaseAccess("view-realm") || hasBaseAccess("manage-realm"):
          extendedAccess.push("custom-view-realm");

        case hasBaseAccess("view-clients") || hasBaseAccess("manage-clients"):
          extendedAccess.push("custom-view-clients");

        case hasBaseAccess("view-users") ||
          hasBaseAccess("manage-users") ||
          hasBaseAccess("manage-clients"):
          extendedAccess.push("custom-view-users");

        case hasBaseAccess("view-events") ||
          hasBaseAccess("manage-events") ||
          hasBaseAccess("manage-clients"):
          extendedAccess.push("custom-view-events");

        case hasBaseAccess("view-identity-providers") ||
          hasBaseAccess("manage-identity-providers"):
          extendedAccess.push("custom-view-identity-providers");

        case hasBaseAccess("view-authorization") ||
          hasBaseAccess("manage-authorization"):
          extendedAccess.push("custom-view-authorization");

        case hasBaseAccess("query-users") ||
          hasExtendedAccess("custom-view-users"):
          extendedAccess.push("custom-query-users");

        case hasBaseAccess("query-groups") ||
          hasExtendedAccess("custom-view-users"):
          extendedAccess.push("custom-query-groups");

        case hasBaseAccess("query-clients") ||
          hasExtendedAccess("custom-view-clients"):
          extendedAccess.push("custom-query-clients");

        default:
          break;
      }

      return extendedAccess;
    };

    const extendedAccess = getExtendedAccess();
    const resultAccess = extendedAccess.concat(realmAccess);

    if (resultAccess) {
      setAccess(resultAccess);
    }
  }, [whoAmI, realm]);

  const getAccessCheckersForTarget = (target: readonly AccessType[]) => {
    return {
      hasAccess: (...types: AccessType[]) => hasAccess(target, ...types),
      hasSomeAccess: (...types: AccessType[]) =>
        hasSomeAccess(target, ...types),
    };
  };

  const getOptionsCheckingAccess = (
    accessOrTypes: readonly AccessType[] | AccessType,
    ...types: AccessType[]
  ) => {
    let resultAccess: readonly AccessType[];
    const resultType = [...types];

    if (isReadonlyArray(accessOrTypes)) {
      resultAccess = accessOrTypes;
    } else {
      resultAccess = access;
      resultType.unshift(accessOrTypes);
    }

    return { resultAccess, resultType };
  };

  const hasAccess = (
    accessOrTypes: readonly AccessType[] | AccessType,
    ...types: AccessType[]
  ) => {
    const { resultAccess, resultType } = getOptionsCheckingAccess(
      accessOrTypes,
      ...types,
    );

    return resultType.every(
      (type) =>
        type === "anyone" ||
        (typeof type === "function" &&
          type({ hasAll: hasAccess, hasAny: hasSomeAccess })) ||
        resultAccess.includes(type),
    );
  };

  const hasSomeAccess = (
    accessOrTypes: readonly AccessType[] | AccessType,
    ...types: AccessType[]
  ) => {
    const { resultAccess, resultType } = getOptionsCheckingAccess(
      accessOrTypes,
      ...types,
    );

    return resultType.some(
      (type) =>
        type === "anyone" ||
        (typeof type === "function" &&
          type({ hasAll: hasAccess, hasAny: hasSomeAccess })) ||
        resultAccess.includes(type),
    );
  };

  const getAccesses = (checkedAccess: AccessType[]) => {
    const result: MapAccess = {};

    checkedAccess.map((item) => {
      if (typeof item !== "function") {
        const accessInCamel = convertCase(item, "kebab", "camel");
        const accessInCapitalizeCamel = capitalizeText(accessInCamel);
        result[`${Literal.WITH}${accessInCapitalizeCamel}${Literal.ACCESS}`] =
          hasAccess(item);
      }
    });

    return result;
  };

  return (
    <AccessContext.Provider value={{ hasAccess, hasSomeAccess, getAccesses }}>
      {children}
    </AccessContext.Provider>
  );
};
