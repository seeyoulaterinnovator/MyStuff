<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
    <#elseif section = "body">
        ${kcSanitize(msg(emailResetPasswordBodyHtml,authHref, expTimePassAndLogin))?no_esc}
        <#if phone??>
            ${kcSanitize(msg(emailLoginAndPhoneHtml, userName, phone))?no_esc}
        <#else>
            ${kcSanitize(msg(emailLoginHtml, userName))?no_esc}
        </#if>
        ${kcSanitize(msg("login"))?no_esc}
        ${kcSanitize(msg(emailPasswordFooterHtml, expTimePass))?no_esc}
    </#if>
</@template.layout>