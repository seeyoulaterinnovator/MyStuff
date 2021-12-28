<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
    <#elseif section = "body">
        <#assign email=realmName>
        <#if user?? && user.getEmail??>
            <#assign email= user.getEmail()>
        </#if>
        ${kcSanitize(msg("identityProviderLinkBodyHtml", identityProviderAlias, email, identityProviderContext.username, link, linkExpiration, linkExpirationFormatter(linkExpiration)))?no_esc}
    </#if>
</@template.layout>
