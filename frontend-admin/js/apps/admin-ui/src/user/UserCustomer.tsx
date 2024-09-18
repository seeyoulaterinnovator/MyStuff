import {useAdminClient} from "../admin-client";
import {useParams} from "../utils/useParams";
import type {UserParams} from "./routes/User";
import {useRealm} from "../context/realm-context/RealmContext";
import {useTranslation} from "react-i18next";
import {Button, PageSection, ToolbarItem} from "@patternfly/react-core";
import {KeycloakDataTable} from "../components/table-toolbar/KeycloakDataTable";
import {ListEmptyState} from "../components/list-empty-state/ListEmptyState";
import {
  PersonalAccountRepresentation
} from "@keycloak/keycloak-admin-client/lib/defs/custom/personalAccountRepresentation";
import {UserPostRepresentation} from "@keycloak/keycloak-admin-client/lib/defs/custom/userRepresentation";
import {useAccess} from "../context/access/Access";
import {Link} from "react-router-dom";
import {toAddClient} from "../clients/routes/AddClient";
import {useState} from "react";
import {useWhoAmI} from "../context/whoami/WhoAmI";

import {useFetch} from "../utils/useFetch";
import {UserPostRoleDropdown} from "./user-customer/UserPostRoleDropdown";
import {KeycloakSpinner} from "../components/keycloak-spinner/KeycloakSpinner";
import {UserPostRoleRepresentation} from "@keycloak/keycloak-admin-client/lib/defs/custom/userPostRoleRepresentation";
import {UserPostAccountsMultiInput} from "./user-customer/UserPostAccountsMultiInput";

export type UserPostFullRepresentation = UserPostRepresentation & {
  accounts: PersonalAccountRepresentation[];
}

const ToolbarItems = () => {
  const { realm } = useRealm();
  const { hasAccess } = useAccess();
  const { t } = useTranslation();

  const canCreate = hasAccess("manage-users") && (realm === "master" || hasAccess("button-add-customer"));

  return (
    <>
      <ToolbarItem>
        <Button
          disabled={!canCreate}
          component={(props) => <Link {...props} to={toAddClient({ realm })} />}
        >
          {t("createUserPost")}
        </Button>
      </ToolbarItem>
    </>
  );
};

export const UserCustomer = () => {
  const {adminClient} = useAdminClient();
  const { id } = useParams<UserParams>();
  const { realm } = useRealm();
  const { whoAmI } = useWhoAmI();
  const { hasAccess } = useAccess();
  const { t } = useTranslation();

  const [roles, setRoles] = useState<UserPostRoleRepresentation[] | null>(null);
  const [tableKey, setTableKey] = useState(0);

  const refresh = () => setTableKey(prevKey => prevKey + 1);

  const canDelete = hasAccess("manage-users")
    && (whoAmI.getRealm() === "master" || hasAccess("button-delete-customer"));

  const canEdit = hasAccess("manage-users")
    && (whoAmI.getRealm() === "master" || hasAccess("edit-customer"));

  useFetch(
    async () => {
      return (await adminClient.userPosts.findUserPostRoles({ realm })).results.roles;
    },
    setRoles,
    [realm],
  );

  const loader = async () => {
    const postsResponse = await adminClient.userPosts.findUserPosts({ userId: id, realm });
    const posts = postsResponse.results.user_post;
    const postAccountsResponses = (await Promise.all(
      posts.map(post => adminClient.personalAccounts.findPersonalAccounts({
        postId: post.id,
        realm
      }))
    ));
    const accountDatas = postAccountsResponses.flatMap(response => response.results.data);
    return posts.map(post => ({
      ...post,
      accounts: accountDatas.filter(data => data.post_id === post.id)
        .flatMap(data => data.accounts)
    } as UserPostFullRepresentation));
  };

  if (!roles) return <KeycloakSpinner />;

  return (
    <PageSection variant="light" className="pf-v5-u-p-0">
      <KeycloakDataTable
        key={tableKey}
        loader={loader}
        ariaLabelKey="titleCustomer"
        columns={[{
          name: "selected",
          displayKey: "currentSelect",
          cellRenderer: (post) => `${post.selected ? t('selected') : ''}`
        },{
          name: "id",
          displayKey: "id",
          cellRenderer: (post) => <span style={{wordWrap: 'break-word'}}>{post.id}</span>
        }, {
          name: "organization",
          displayKey: "organization"
        }, {
          name: "account",
          displayKey: "account",
          cellRenderer: (post) => (
            <UserPostAccountsMultiInput
              userPostId={post.id}
              accounts={post.accounts}
              onChange={refresh}
              isReadonly={!canEdit}
            />
          )
        }, {
          name: "tomsId",
          displayKey: "tomsId",
          cellRenderer: (post) => <span style={{wordWrap: 'break-word'}}>{post.tomsId}</span>
        }, {
          name: "dmpId",
          displayKey: "dmpId",
          cellRenderer: (post) => <span style={{wordWrap: 'break-word'}}>{post.dmpId}</span>
        }, {
          name: "role",
          displayKey: "role",
          cellRenderer: (post) =>  (
            <UserPostRoleDropdown
              userPostId={post.id}
              defaultUserPostRoleId={post.userRole.id}
              userPostRoles={roles}
              onChange={refresh}
              isReadonly={!canEdit}
            />
          )
        }, {
          name: "systems",
          displayKey: "systems",
        }, {
          name: "",
          displayKey: " ",
          cellRenderer: (post) => {
            return (
              <Button
                style={{float: 'right'}}
                type="button"
                disabled={!canDelete}
                onClick={async () => {
                  // TODO
                }}
              >
                {t("delete")}
              </Button>
            );
          }
        }]}
        emptyState={
          <ListEmptyState
            message={t("emptyUserPosts")}
            instructions={t("noUserPostsInstructions")}
          />
        }
        toolbarItem={<ToolbarItems/>}
      />
    </PageSection>
  );
}
