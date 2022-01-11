<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
        <title>${kcSanitize(msg("emailDisabledAccountSubject"))}</title>
    <#elseif section = "body">
        ${kcSanitize(msg("emailDisabledAccountBodyHtml", userName, userFirstName, userLastName, accountLink))?no_esc}
    </#if>
</@template.layout>
