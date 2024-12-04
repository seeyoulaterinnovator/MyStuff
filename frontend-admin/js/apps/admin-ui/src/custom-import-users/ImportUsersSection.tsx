import { PageSection } from "@patternfly/react-core";
import { ViewHeader } from "../components/view-header/ViewHeader";
import { ImportUsersDataTable } from "../components/custom/import-users/ImportUsersDataTable";

import "./import-users-section.css";

export default function ImportUsersSection() {
  return (
    <>
      <ViewHeader
        titleKey="titleImportUsers"
        subKey="subtitleImportUsers"
        divider={false}
      />
      <PageSection
        data-testid="import-users-page"
        variant="light"
        className="pf-v5-u-p-0"
      >
        <ImportUsersDataTable />
      </PageSection>
    </>
  );
}
