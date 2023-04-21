<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
        <title>${kcSanitize(msg("emailCredentialDisableSubject"))}</title>
    <#elseif section = "body">
        ${kcSanitize(msg("emailCredentialDisableBodyHtml", authHref))?no_esc}
    </#if>
</@template.layout>
