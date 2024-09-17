import {useAdminClient} from "../admin-client";
import {useParams} from "../utils/useParams";
import type {UserParams} from "./routes/User";
import {useRealm} from "../context/realm-context/RealmContext";
import {useTranslation} from "react-i18next";
import {Button, PageSection} from "@patternfly/react-core";
import {KeycloakDataTable} from "../components/table-toolbar/KeycloakDataTable";
import {ListEmptyState} from "../components/list-empty-state/ListEmptyState";
import {
  PersonalAccountRepresentation
} from "@keycloak/keycloak-admin-client/lib/defs/custom/personalAccountRepresentation";
import {UserPostRepresentation} from "@keycloak/keycloak-admin-client/lib/defs/custom/userRepresentation";

export type UserPostFullRepresentation = UserPostRepresentation & {
  accounts: PersonalAccountRepresentation[];
}

const AccountCell = ({ post } : { post: UserPostFullRepresentation }) => {
  // TODO
  return (<></>);
};

const RoleCell = ({ post } : { post: UserPostFullRepresentation }) => {
  // TODO
  return (<></>);
};

export const UserCustomer = () => {
  const {adminClient} = useAdminClient();

  const { id } = useParams<UserParams>();
  const { realm } = useRealm();
  const { t } = useTranslation();

  const loader = async () => {
    const postsResponse = await adminClient.userPosts.findUserPosts({ userId: id, realm });
    const posts = postsResponse.results.user_post;
    const postAccountsResponses = (await Promise.all(
      posts.map(post => adminClient.personalAccounts.findPersonalAccounts({
        postId: post.id,
        realm
      }))
    ));
    const accounts = postAccountsResponses.flatMap(response => response.results.data.accounts);
    return posts.map(post => ({
      ...post,
      accounts: accounts.filter(account => account.post_id === post.id)
    } as UserPostFullRepresentation));
  };

  return (
    <PageSection variant="light" className="pf-v5-u-p-0">
      <KeycloakDataTable
        loader={loader}
        ariaLabelKey="titleCustomer"
        columns={[{
          name: "selected",
          displayKey: "currentSelect",
          cellRenderer: (post) => `${post.selected ? t('selected') : ''}`
        },{
          name: "id",
          displayKey: "id"
        }, {
          name: "organization",
          displayKey: "organization"
        }, {
          name: "account",
          displayKey: "account",
          cellRenderer: (post) => <AccountCell post={post} />
        }, {
          name: "tomsId",
          displayKey: "tomsId"
        }, {
          name: "dmpId",
          displayKey: "dmp"
        }, {
          name: "role",
          displayKey: "role",
          cellRenderer: (post) => <RoleCell post={post} />
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
      />
    </PageSection>
  );
}
