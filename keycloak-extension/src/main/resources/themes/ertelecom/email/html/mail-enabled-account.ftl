<#import "template.ftl" as template>
<#import "./helpers/blocks.ftl" as blocks>

<#--  Admin console - Users – Select user – Unlock Users -->
<#--  Admin console - Users – Visit user's card – Enabled/Disabled -->
<@template.layout ; section>
  <#if section = "style">
    <title>${kcSanitize(msg(emailEnabledAccountSubject!""))}</title>
  <#elseif section = "body">
    <@blocks.parameterizedMsg message=emailEnabledAccountBodyHtml/>
  </#if>
</@template.layout>

<#--  TODO: Old logic  -->
<#--  <#import "template.ftl" as template>

<@template.layout ; section>
  <#if section = "style">
    <#include 'styles/content-style--default.html' >
    <title>${kcSanitize(msg("emailEnabledAccountSubject"))}</title>
  <#elseif section = "body">
    ${kcSanitize(msg("emailEnabledAccountBodyHtml", userName, userFirstName, userLastName, accountLink))?no_esc}
  </#if>
</@template.layout>  -->
