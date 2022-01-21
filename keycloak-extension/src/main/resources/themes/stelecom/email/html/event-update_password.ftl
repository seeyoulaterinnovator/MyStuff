<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
        <title>${kcSanitize(msg("eventUpdatePasswordSubject"))}</title>
    <#elseif section = "body">
        ${kcSanitize(msg("eventUpdatePasswordBodyHtml",event.date, event.ipAddress))?no_esc}
    </#if>
</@template.layout>
