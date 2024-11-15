<#import "template.ftl" as template>

<#-- ??? way for getting -->
<#--  not used  -->
<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
        <title>${kcSanitize(msg("eventUpdateTotpSubject"))}</title>
    <#elseif section = "body">
        ${kcSanitize(msg("eventUpdateTotpBodyHtml",event.date, event.ipAddress))?no_esc}
    </#if>
</@template.layout>
