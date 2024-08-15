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
  <#elseif section = "body">

    <#assign email=realmName>
    <#if user?? && user.getEmail??>
      <#assign email= user.getEmail()>
    </#if>

    ${kcSanitize(msg(executeActionsBodyHtml,link, linkExpiration, email, requiredActionsText, linkExpirationFormatter(linkExpiration), time))?no_esc}
  </#if>
</@template.layout>
