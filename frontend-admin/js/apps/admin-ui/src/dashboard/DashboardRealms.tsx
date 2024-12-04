import { PageSection } from "@patternfly/react-core";
import { ViewHeader } from "../components/view-header/ViewHeader";
import { RealmsDataTable } from "../components/custom/realms/RealmsDataTable";

export const DashboardRealms = () => {
  return (
    <>
      <ViewHeader titleKey="realms" divider={false} />
      <PageSection
        data-testid="dashboard-realms-page"
        variant="light"
        className="pf-v5-u-p-0"
      >
        <RealmsDataTable />
      </PageSection>
    </>
  );
};
