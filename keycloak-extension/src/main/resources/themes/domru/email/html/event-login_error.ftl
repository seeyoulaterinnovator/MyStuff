<#import "template.ftl" as template>

<#-- ЛК - Ввести неверный логопас -->
<#--  not used  -->
<@template.layout ; section>
    <#if section = "style">
      <title>${kcSanitize(msg(eventLoginErrorSubject!""))}</title>
    <#elseif section = "body">
        ${kcSanitize(msg("eventLoginErrorBodyHtml",event.date,event.ipAddress))?no_esc}
    </#if>
</@template.layout>
