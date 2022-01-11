<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
        <title>${kcSanitize(msg("emailVerificationSubject"))}</title>
    <#elseif section = "body">
        <#assign email=realmName>
            <#if user?? && user.getEmail??>
                <#assign email= user.getEmail()>
            </#if>
        ${kcSanitize(msg("emailVerificationBodyHtml",link, linkExpiration, email, linkExpirationFormatter(linkExpiration), expTime))?no_esc}
    </#if>
</@template.layout>
