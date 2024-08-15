<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
    <#elseif section = "body">
        ${kcSanitize(msg(emailCredentialDisableBodyHtmlCost, authHref, expTime, email, phone))?no_esc}
    </#if>
</@template.layout>
