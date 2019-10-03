<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "header">
    <#elseif section = "form">
        <#assign email=realm.displayName>
        <#if brokerContext?? && brokerContext.email??>
            <#assign email= brokerContext.email>
        </#if>

      <p id="instruction1" class="instruction">
            ${msg("emailLinkIdp", email)}
        </p>
    </#if>
</@layout.registrationLayout>