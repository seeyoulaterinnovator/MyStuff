<#import "template.ftl" as template>
<#import "./helpers/blocks.ftl" as blocks>

<#-- Admin console - Users - Visit user's card - Credentials - Credential Reset  - Verify Email - Письмо executeActions.ftl - Обновить данные-->
<@template.layout ; section>
  <#if section="style">
    <title>${kcSanitize(msg(emailVerificationSubject!""))}</title>
  <#elseif section="body">
    <@blocks.parameterizedMsg message=emailVerificationLoginBodyHtml/>
  </#if>
</@template.layout>

<#--  TODO: Old logic  -->
<#--  <#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
        <title>${kcSanitize(msg("emailVerificationSubject"))}</title>
    <#elseif section = "body">
        ${kcSanitize(msg("emailVerificationLoginBodyHtml",link, linkExpiration, realmName, linkExpirationFormatter(linkExpiration), expTime))?no_esc}
    </#if>
</@template.layout>  -->
