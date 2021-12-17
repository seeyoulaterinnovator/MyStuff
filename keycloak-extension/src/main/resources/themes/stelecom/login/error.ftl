<#import "template.ftl" as layout>
<#import "templates/error.ftl" as errorPage>

<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        <#--  ${msg("errorTitle")}  -->
    <#elseif section = "form">
        <#if client?? && client.baseUrl?has_content> 
          <#assign backHref="${client.baseUrl}" backMessage="${kcSanitize(msg('backToApplication'))?no_esc}" >
        </#if>

        <@errorPage.codeAll backHref=backHref backMessage=backMessage />
    </#if>
</@layout.registrationLayout>