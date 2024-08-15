<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
    <#elseif section = "body">
        ${kcSanitize(msg("eventLoginErrorBodyHtml",event.date,event.ipAddress))?no_esc}
    </#if>
</@template.layout>
