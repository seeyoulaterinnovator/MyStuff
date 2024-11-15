<#import "template.ftl" as template>

<#-- ??? ЛК - Забыли пароль? - Установить новый пароль  -->
<#--  not used  -->
<@template.layout ; section>
    <#if section = "style">
      <title>${kcSanitize(msg(eventUpdatePasswordSubject!""))}</title>
    <#elseif section = "body">
        ${kcSanitize(msg("eventUpdatePasswordBodyHtml",event.date, event.ipAddress))?no_esc}
    </#if>
</@template.layout>
