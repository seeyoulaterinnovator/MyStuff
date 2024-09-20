import {useAdminClient} from "../../admin-client";
import {useRealm} from "../../context/realm-context/RealmContext";
import {useWhoAmI} from "../../context/whoami/WhoAmI";
import {useAccess} from "../../context/access/Access";
import {useTranslation} from "react-i18next";
import {useAlerts} from "../../components/alert/Alerts";
import {MutableRefObject, useState} from "react";
import {AlertVariant, Button} from "@patternfly/react-core";

type UserPostActionsProps = {
  userId: string;
  postId: string | null;
  newTomsIdRef: MutableRefObject<string>,
  newDmpIdRef: MutableRefObject<string>,
  newRoleIdRef: MutableRefObject<number>,
  onChanged: () => void;
}

export const UserPostActions = (props: UserPostActionsProps) => {
  const {
    userId,
    postId,
    newTomsIdRef,
    newDmpIdRef,
    newRoleIdRef,
    onChanged,
  } = props;

  const { adminClient } = useAdminClient();
  const { realm } = useRealm();
  const { whoAmI } = useWhoAmI();
  const { hasAccess } = useAccess();
  const { t } = useTranslation();
  const { addAlert, addError } = useAlerts();

  const canDelete =
    hasAccess("manage-users") &&
    (whoAmI.getRealm() === "master" || hasAccess("button-delete-customer"));

  const [isChanging, setIsChanging] = useState(false);

  return postId !== null ? (
    <Button
      style={{ float: "right" }}
      type="button"
      disabled={!canDelete || isChanging}
      isLoading={isChanging}
      onClick={async () => {
        setIsChanging(true);
        try {
          await adminClient.userPosts.deleteUserPost({
            realm,
            postId,
          });
          addAlert(
            t("deleteUserPostSuccess", { postId }),
            AlertVariant.success,
          );
          onChanged();
        } catch (error) {
          addError(
            t("deleteUserPostError", { postId }),
            error,
          );
        } finally {
          setIsChanging(false);
        }
      }}
    >
      {t("delete")}
    </Button>
  ) : (
    <Button
      style={{ float: "right" }}
      type="button"
      disabled={isChanging}
      isLoading={isChanging}
      onClick={async () => {
        setIsChanging(true);
        try {
          await adminClient.userPosts.createUserPost(
            { realm },
            {
              userId,
              dmpId: newDmpIdRef.current,
              tomsId: newTomsIdRef.current,
              roleId: newRoleIdRef.current,
            },
          );
          addAlert(
            t("addUserPostSuccess", { postId }),
            AlertVariant.success,
          );
          onChanged();
        } catch (error) {
          addError(t("addUserPostError", { postId }), error);
        } finally {
          setIsChanging(false);
        }
      }}
    >
      {t("add")}
    </Button>
  );
}
