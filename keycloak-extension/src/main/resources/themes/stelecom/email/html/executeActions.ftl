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
<#import "./helpers/blocks.ftl" as blocks>

<#-- Admin console - Users - Visit user's card - Credentials - Credential Reset -->
<@template.layout ; section>
  <#if section="style">
    <title>${kcSanitize(msg(executeActionsSubject!""))}</title>
  <#elseif section="body">
    <@blocks.parameterizedMsg message=executeActionsBodyHtml/>
  </#if>
</@template.layout>

<#--  TODO: Old logic  -->
<#--  <#import "template.ftl" as template>

<@template.layout ; section>
  <#if section = "style">
    <#include 'styles/content-style--default.html' >
    <title>${kcSanitize(msg("executeActionsSubject"))}</title>
  <#elseif section = "body">

  <#assign email=realmName>
  <#if user?? && user.getEmail??>
  <#assign email= user.getEmail()>
  </#if>
 
    ${kcSanitize(msg("executeActionsBodyHtml",link, linkExpiration, email, requiredActionsText, linkExpirationFormatter(linkExpiration), time))?no_esc}
  </#if>
</@template.layout>  -->
