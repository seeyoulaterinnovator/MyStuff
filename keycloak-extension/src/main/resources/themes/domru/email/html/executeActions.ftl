<#outputformat "plainText">
  <#assign requiredActionsText>
    <#if requiredActions??>
      <#list requiredActions>
        <#items as reqActionItem>
          ${msg("requiredAction.${reqActionItem}")}<#sep>, </#sep>
        </#items>
      </#list>
    </#if>
  </#assign>
</#outputformat>

<#import "template.ftl" as template>

<@template.layout ; section>
  <#if section = "style">
    <#include 'styles/content-style--default.html' >
  <#elseif section = "body">
    ${kcSanitize(msg("executeActionsBodyHtml",link, linkExpiration, realmName, requiredActionsText, linkExpirationFormatter(linkExpiration)))?no_esc}
  </#if>
</@template.layout>
