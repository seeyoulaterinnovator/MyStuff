import {PageSection, Tab, TabTitleText} from "@patternfly/react-core";
import {useTranslation} from "react-i18next";
import {useRealm} from "../context/realm-context/RealmContext";
import {ViewHeader} from "../components/view-header/ViewHeader";
import {RoutableTabs, useRoutableTab} from "../components/routable-tabs/RoutableTabs";
import {CustomSettingsTab, toCustomSettings} from "./routes/CustomSettings";
import CustomSettingsTable from "./CustomSettingsTable";

export default function CustomSettingsSection() {
  const { t } = useTranslation();
  const { realm } = useRealm();

  const useTab = (tab: CustomSettingsTab) => useRoutableTab(toCustomSettings({ realm, tab }));

  const generalTab = useTab("general");
  const frontTab = useTab("front");
  const messageTab = useTab("message");
  const applicationTab = useTab("application");
  const gatewayTab = useTab("gateway");

  return (
    <>
      <ViewHeader
        titleKey="titleCustomSettings"
        divider={false}
      />
      <PageSection variant="light" className="pf-v5-u-p-0">
        <RoutableTabs
          isBox
          defaultLocation={toCustomSettings({ realm, tab: "general" })}
        >
          <Tab
            title={<TabTitleText>{t("customSettingsGeneral")}</TabTitleText>}
            {...generalTab}
          >
            <CustomSettingsTable type="REALM"/>
          </Tab>
          <Tab
            title={<TabTitleText>{t("customSettingsFront")}</TabTitleText>}
            {...frontTab}
          >
            <CustomSettingsTable type="FRONT"/>
          </Tab>
          <Tab
            title={<TabTitleText>{t("customSettingsMessage")}</TabTitleText>}
            {...messageTab}
          >
            <CustomSettingsTable type="MESSAGE"/>
          </Tab>
          <Tab
            title={<TabTitleText>{t("customSettingsApplication")}</TabTitleText>}
            {...applicationTab}
          >
            <CustomSettingsTable type="APP"/>
          </Tab>
          <Tab
            title={<TabTitleText>{t("customSettingsGateway")}</TabTitleText>}
            {...gatewayTab}
          >
            <CustomSettingsTable type="EMAIL"/>
          </Tab>
        </RoutableTabs>
      </PageSection>
    </>
  );
}
