<#import "template.ftl" as template>
<#import "./helpers/blocks.ftl" as blocks>

<#--  Admin console - Users – Select user – Send login -->
<@template.layout ; section>
  <#if section="style">
    <title>${kcSanitize(msg(emailSendLoginSubject!""))}</title>
  <#elseif section="body">
    <@blocks.parameterizedMsg message=emailSendLoginBodyHtml/>
  </#if>
</@template.layout>

<#--  TODO: Old logic  -->
<#--  <#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
        <title>${kcSanitize(msg("emailSendLoginSubject"))}</title>
    <#elseif section = "body">
        ${kcSanitize(msg("emailSendLoginBodyHtml"))?no_esc}
        <#if phone??>
            ${kcSanitize(msg("emailLoginAndPhoneHtml", userName, phone))?no_esc}
        <#else>
            ${kcSanitize(msg("emailLoginHtml", userName))?no_esc}
        </#if>
        ${kcSanitize(msg("emailPasswordFooterHtml"))?no_esc}
    </#if>
</@template.layout>  -->
