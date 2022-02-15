<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
    <#elseif section = "body">
        ${kcSanitize(msg(emailDisabledAccountBodyHtml))?no_esc}
        <#if phone??>
            ${kcSanitize(msg(emailLoginAndPhoneHtml, userName, phone))?no_esc}
        <#else>
            ${kcSanitize(msg(emailLoginHtml, userName))?no_esc}
        </#if>
        ${kcSanitize(msg(emailPasswordFooterHtml, expTimePass))?no_esc}
    </#if>
</@template.layout>