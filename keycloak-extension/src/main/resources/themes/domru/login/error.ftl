<#import "template.ftl" as layout>
<#import "templates/error.ftl" as errorPage>

<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        <#--  ${msg("errorTitle")}  -->
    <#elseif section = "form">
        <#if client?? && client.baseUrl?has_content> 
          <#assign backHref="${client.baseUrl}" backMessage="${kcSanitize(msg('backToApplication'))?no_esc}" >
        </#if>

        <#if message.summary?contains('Page not found') || message.summary?contains('Неверный параметр')  >
            <@errorPage.code500 backHref=backHref backMessage=backMessage />
        <#elseif message.summary?contains('500') >
            <@errorPage.code404 backHref=backHref backMessage=backMessage />
        <#else>
            ${message.summary}
            <#if client?? && client.baseUrl?has_content>
                <p><a id="backToApplication" href="${client.baseUrl}">${kcSanitize(msg("backToApplication"))?no_esc}</a></p>
            </#if>
        </#if>
    </#if>
</@layout.registrationLayout>