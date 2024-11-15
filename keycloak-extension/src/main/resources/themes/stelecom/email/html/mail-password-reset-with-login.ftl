<#import "template.ftl" as template>
<#import "./helpers/blocks.ftl" as blocks>

<#--  Admin console - Users – Select user – Send login and reset password -->
<@template.layout ; section>
  <#if section="style">
    <title>${kcSanitize(msg(emailResetPasswordSubject!""))}</title>
  <#elseif section="body">
    <@blocks.parameterizedMsg message=emailResetPasswordBodyHtml/>
  </#if>
</@template.layout>

<#--  TODO: Old logic  -->
<#--  <#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
        <title>${kcSanitize(msg("emailResetPasswordSubject"))}</title>
    <#elseif section = "body">
        ${kcSanitize(msg("emailResetPasswordBodyHtml",authHref))?no_esc}
        <#if phone??>
            ${kcSanitize(msg("emailLoginAndPhoneHtml", userName, phone))?no_esc}
        <#else>
            ${kcSanitize(msg("emailLoginHtml", userName))?no_esc}
        </#if>
        ${kcSanitize(msg("emailPasswordFooterHtml"))?no_esc}
    </#if>
</@template.layout>  -->
