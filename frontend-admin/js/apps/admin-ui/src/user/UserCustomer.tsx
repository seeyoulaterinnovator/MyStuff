import { useAdminClient } from "../admin-client";
import { useParams } from "../utils/useParams";
import type { UserParams } from "./routes/User";
import { useRealm } from "../context/realm-context/RealmContext";
import { useTranslation } from "react-i18next";
import {
  AlertVariant,
  Button,
  PageSection,
  TextInput,
  ToolbarItem,
} from "@patternfly/react-core";
import {
  Field,
  KeycloakDataTable,
} from "../components/table-toolbar/KeycloakDataTable";
import { ListEmptyState } from "../components/list-empty-state/ListEmptyState";
import { PersonalAccountRepresentation } from "@keycloak/keycloak-admin-client/lib/defs/custom/personalAccountRepresentation";
import {
  DEFAULT_USER_ROLE_ID,
  SystemRoleRepresentation,
  UserPostRepresentation,
} from "@keycloak/keycloak-admin-client/lib/defs/custom/userRepresentation";
import { useAccess } from "../context/access/Access";
import { useCallback, useMemo, useRef, useState } from "react";
import { useWhoAmI } from "../context/whoami/WhoAmI";

import { useFetch } from "../utils/useFetch";
import { UserPostRoleDropdown } from "./user-customer/UserPostRoleDropdown";
import { KeycloakSpinner } from "../components/keycloak-spinner/KeycloakSpinner";
import { UserPostAccountsMultiInput } from "./user-customer/UserPostAccountsMultiInput";
import { SyncAltIcon } from "@patternfly/react-icons";
import { useAlerts } from "../components/alert/Alerts";
import { UserPostSystemRoleMultiSelect } from "./user-customer/UserPostSystemRoleMultiSelect";
import { UserPostRoleRepresentation } from "@keycloak/keycloak-admin-client/lib/defs/custom/userPostRepresentation";

import "./user-customer.css";

type Post =
  | (UserPostRepresentation & {
      accounts: PersonalAccountRepresentation[];
    })
  | {
      id: null;
    };

export const UserCustomer = () => {
  const { adminClient } = useAdminClient();
  const { id: userId } = useParams<UserParams>();
  const { realm } = useRealm();
  const { whoAmI } = useWhoAmI();
  const { hasAccess } = useAccess();
  const { t } = useTranslation();
  const { addAlert, addError } = useAlerts();

  const [key, setKey] = useState(0);
  const [posts, setPosts] = useState<Post[] | null>(null);
  const [roles, setRoles] = useState<UserPostRoleRepresentation[] | null>(null);
  const [systemRoles, setSystemRoles] = useState<
    SystemRoleRepresentation[] | null
  >(null);
  const [isChanging, setIsChanging] = useState(false);

  const newTomsIdRef = useRef("");
  const newDmpIdRef = useRef("");
  const newRoleIdRef = useRef<number>(DEFAULT_USER_ROLE_ID);

  const canCreate =
    hasAccess("manage-users") &&
    (whoAmI.getRealm() || hasAccess("button-add-customer"));

  const canEdit =
    hasAccess("manage-users") &&
    (whoAmI.getRealm() === "master" || hasAccess("edit-customer"));

  const canDelete =
    hasAccess("manage-users") &&
    (whoAmI.getRealm() === "master" || hasAccess("button-delete-customer"));

  const refresh = useCallback(() => {
    setRoles(null);
    setPosts(null);
    setKey((prevKey) => prevKey + 1);
  }, []);

  useFetch(
    async () => {
      return (await adminClient.userPosts.findUserPostRoles({ realm })).results
        .roles;
    },
    setRoles,
    [realm, key],
  );

  useFetch(
    async () => {
      return (
        await adminClient.userPosts.findUserPostSystemRoles({
          realm,
          realmId: realm,
        })
      ).results["system-roles"];
    },
    setSystemRoles,
    [realm, key],
  );

  useFetch(
    async () => {
      newTomsIdRef.current = "";
      newDmpIdRef.current = "";
      newRoleIdRef.current = DEFAULT_USER_ROLE_ID;
      const postsResponse = await adminClient.userPosts.findUserPosts({
        userId,
        realm,
      });
      const posts = postsResponse.results.user_post;
      const postAccountsResponses = await Promise.all(
        posts.map((post) =>
          adminClient.personalAccounts.findPersonalAccounts({
            postId: post.id,
            realm,
          }),
        ),
      );
      const accountDataList = postAccountsResponses.flatMap(
        (response) => response.results.data,
      );
      return posts
        .map(
          (post) =>
            ({
              ...post,
              accounts: accountDataList
                .filter((data) => data.post_id === post.id)
                .flatMap((data) => data.accounts),
            }) as Post,
        )
        .concat(
          canCreate
            ? [
                {
                  id: null,
                },
              ]
            : [],
        );
    },
    setPosts,
    [key],
  );

  const columns = useMemo<Field<Post>[]>(() => {
    if (!roles || !systemRoles) return [];

    return [
      {
        name: "selected",
        displayKey: "currentSelect",
        cellRenderer: (post) =>
          post.id !== null ? `${post.selected ? t("selected") : ""}` : "",
      },
      {
        name: "id",
        displayKey: "id",
        cellRenderer: (post) => (post.id !== null ? post.id : ""),
      },
      {
        name: "organization",
        displayKey: "organization",
      },
      {
        name: "account",
        displayKey: "account",
        cellRenderer: (post) =>
          post.id !== null ? (
            <UserPostAccountsMultiInput
              postId={post.id}
              defaultAccounts={post.accounts}
              isReadonly={!canEdit || isChanging}
            />
          ) : (
            ""
          ),
      },
      {
        name: "tomsId",
        displayKey: "tomsId",
        cellRenderer: (post) =>
          post.id !== null ? (
            post?.tomsId
          ) : (
            <TextInput
              aria-label={t("tomsId")}
              onChange={(_event, value) => {
                newTomsIdRef.current = value;
              }}
              isDisabled={isChanging}
              placeholder={t("tomsId")}
            />
          ),
      },
      {
        name: "dmpId",
        displayKey: "dmpId",
        cellRenderer: (post) =>
          post.id !== null ? (
            post.dmpId
          ) : (
            <TextInput
              aria-label={t("dmpId")}
              onChange={(_event, value) => {
                newDmpIdRef.current = value;
              }}
              isDisabled={isChanging}
              placeholder={t("dmpId")}
            />
          ),
      },
      {
        name: "role",
        displayKey: "role",
        cellRenderer: (post) =>
          post.id !== null ? (
            <UserPostRoleDropdown
              postId={post.id}
              defaultRoleId={post.userRole.id}
              roles={roles}
              isReadonly={!canEdit || isChanging}
            />
          ) : (
            <UserPostRoleDropdown
              defaultRoleId={DEFAULT_USER_ROLE_ID}
              roles={roles}
              onChange={(role) => {
                newRoleIdRef.current = role.id;
              }}
              isReadonly={!canEdit || isChanging}
            />
          ),
      },
      {
        name: "systemRoles",
        displayKey: "systems",
        cellRenderer: (post) =>
          post.id !== null ? (
            <UserPostSystemRoleMultiSelect
              postId={post.id}
              allSystemRoles={systemRoles}
              defaultSystemRoles={post.systemRoles}
              isReadonly={!canEdit || isChanging}
            />
          ) : (
            ""
          ),
      },
      {
        name: "",
        displayKey: " ",
        cellRenderer: (post) => {
          return post.id !== null ? (
            <Button
              style={{ float: "right" }}
              type="button"
              disabled={!canDelete || isChanging}
              onClick={async () => {
                setIsChanging(true);
                try {
                  await adminClient.userPosts.deleteUserPost({
                    realm,
                    postId: post.id,
                  });
                  addAlert(
                    t("deleteUserPostSuccess", { postId: post.id }),
                    AlertVariant.success,
                  );
                  refresh();
                } catch (error) {
                  addError(
                    t("deleteUserPostError", { postId: post.id }),
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
                    t("addUserPostSuccess", { postId: post.id }),
                    AlertVariant.success,
                  );
                  refresh();
                } catch (error) {
                  addError(t("addUserPostError", { postId: post.id }), error);
                } finally {
                  setIsChanging(false);
                }
              }}
            >
              {t("add")}
            </Button>
          );
        },
      },
    ];
  }, [
    roles,
    t,
    isChanging,
    addAlert,
    addError,
    adminClient.userPosts,
    realm,
    userId,
    refresh,
    systemRoles,
  ]);

  if (!roles || !systemRoles || !posts) return <KeycloakSpinner />;

  return (
    <PageSection variant="light" className="pf-v5-u-p-0">
      <KeycloakDataTable
        loader={posts}
        ariaLabelKey="titleCustomer"
        columns={columns}
        onlyTable={true}
        emptyState={
          <ListEmptyState
            message={t("emptyUserPosts")}
            instructions={t("noUserPostsInstructions")}
          />
        }
        onlyTableToolbarItem={
          <>
            <ToolbarItem variant="separator" />{" "}
            <ToolbarItem>
              <Button variant="link" onClick={refresh}>
                <SyncAltIcon /> {t("refresh")}
              </Button>
            </ToolbarItem>
          </>
        }
        className="kc-user-customer-table"
      />
    </PageSection>
  );
};
