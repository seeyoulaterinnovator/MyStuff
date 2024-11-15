<#import "template.ftl" as template>

<#-- Admin console - Authentication – Polices – Expire password -->
<#--  not used  -->
<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
        <title>${kcSanitize(msg("emailExpiresPasswordDataSubject"))}</title>
    <#elseif section = "body">
        ${kcSanitize(msg("emailExpiresPasswordDataBodyHtml", link))?no_esc}
    </#if>
</@template.layout>
