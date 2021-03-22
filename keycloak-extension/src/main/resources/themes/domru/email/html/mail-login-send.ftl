<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
    <#elseif section = "body">
        ${kcSanitize(msg("emailSendLoginBodyHtml", userName, phone))?no_esc}
        ${kcSanitize(msg("emailPasswordFooterHtml"))?no_esc}
    </#if>
</@template.layout>