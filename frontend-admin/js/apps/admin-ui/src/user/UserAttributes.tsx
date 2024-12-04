import type UserRepresentation from "@keycloak/keycloak-admin-client/lib/defs/userRepresentation";
import { PageSection, PageSectionVariants } from "@patternfly/react-core";
import { UseFormReturn, useFormContext } from "react-hook-form";

import {
  AttributeForm,
  AttributesForm,
} from "../components/key-value-form/AttributeForm";
import { UserFormFields, toUserFormFields } from "./form-state";
import {
  UnmanagedAttributePolicy,
  UserProfileConfig,
} from "@keycloak/keycloak-admin-client/lib/defs/userProfileMetadata";
import { useAccess } from "../context/access/Access";
import { useWhoAmI } from "../context/whoami/WhoAmI";
import { useCustomConfig } from "../customLogic/context/CustomConfigContext";

type UserAttributesProps = {
  user: UserRepresentation;
  save: (user: UserFormFields) => void;
  upConfig?: UserProfileConfig;
};

export const UserAttributes = ({
  user,
  save,
  upConfig,
}: UserAttributesProps) => {
  const form = useFormContext<UserFormFields>();
  const { isCustomTheme } = useCustomConfig();
  const { isMeInMaster } = useWhoAmI();
  const { getAccesses } = useAccess();
  const { withManageUsersAccess, withEditAttributesAccess } = getAccesses([
    "manage-users",
    "edit-attributes",
  ]);

  return (
    <PageSection variant={PageSectionVariants.light}>
      <AttributesForm
        form={form as UseFormReturn<AttributeForm>}
        save={save}
        fineGrainedAccess={user.access?.manage}
        reset={() =>
          form.reset({
            ...form.getValues(),
            attributes: toUserFormFields(user).attributes,
          })
        }
        name="unmanagedAttributes"
        isDisabled={
          UnmanagedAttributePolicy.AdminView ==
          upConfig?.unmanagedAttributePolicy
        }
        isReadonly={
          isCustomTheme &&
          !(withManageUsersAccess && (isMeInMaster || withEditAttributesAccess))
        }
      />
    </PageSection>
  );
};
