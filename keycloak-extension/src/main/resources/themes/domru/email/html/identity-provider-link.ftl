<#import "template.ftl" as template>

<#-- ??? Admin console - Users - Visit user's card - Identity provider links -->
<#--  not used  -->
<@template.layout ; section>
    <#if section = "style">
        <title>${kcSanitize(msg(identityProviderLinkSubject!"", customer))}</title>
    <#elseif section = "body">
        <#assign email=realmName>
        <#if user?? && user.getEmail??>
            <#assign email= user.getEmail()>
        </#if>
        ${kcSanitize(msg(identityProviderLinkBodyHtml, identityProviderAlias, email, identityProviderContext.username, link, linkExpiration, linkExpirationFormatter(linkExpiration)))?no_esc}
    </#if>
</@template.layout>
