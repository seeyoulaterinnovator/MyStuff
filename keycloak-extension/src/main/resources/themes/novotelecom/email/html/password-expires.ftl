<#import "template.ftl" as template>

<#-- Admin console - Authentication – Polices – Expire password -->
<#--  not used  -->
<@template.layout ; section>
    <#if section = "style">
        <title>${kcSanitize(msg(emailExpiresPasswordDataSubject!""))}</title>
    <#elseif section = "body">
        ${passwordExpiresSchedulerHtml?no_esc}
    </#if>
</@template.layout>
