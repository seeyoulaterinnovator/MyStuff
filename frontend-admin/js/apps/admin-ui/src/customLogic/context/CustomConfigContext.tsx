import {
  createNamedContext,
  useRequiredContext,
} from "@keycloak/keycloak-ui-shared";
import { PropsWithChildren, useState } from "react";
import { useAdminClient } from "../../admin-client";
import { useFetch } from "../../utils/useFetch";
import { useRealm } from "../../context/realm-context/RealmContext";
import { CustomConfigRepresentation } from "@keycloak/keycloak-admin-client/lib/defs/custom/customConfigRepresentation";
import { AdminTheme } from "../constants/theme";

type CustomConfigContextType = {
  /**
   * Одноименное свойство из RealmRepresentation может отсутствовать,
   * например, в manager console, поэтому запрашивается отдельно
   */
  adminTheme?: string;
  isCustomTheme: boolean;
};

export const CustomConfigContext = createNamedContext<
  CustomConfigContextType | undefined
>("CustomConfigContext", undefined);

export const CustomConfigContextProvider = ({
  children,
}: PropsWithChildren) => {
  const { adminClient } = useAdminClient();
  const { realm } = useRealm();
  const [config, setConfig] = useState<CustomConfigRepresentation | null>(null);

  useFetch(
    async () => {
      return (
        (await adminClient.customConfig.getCustomConfig({ realm })).results ||
        {}
      );
    },
    setConfig,
    [realm],
  );

  if (!config) return;

  return (
    <CustomConfigContext.Provider
      value={{
        adminTheme: config?.adminTheme,
        isCustomTheme: config?.adminTheme === AdminTheme.KEYCLOAK_V2,
      }}
    >
      {children}
    </CustomConfigContext.Provider>
  );
};

export const useCustomConfig = () => useRequiredContext(CustomConfigContext);
