<#import "template.ftl" as template>
<#import "./helpers/blocks.ftl" as blocks>

<#-- Admin console - Users - Add user -->
<@template.layout ; section>
  <#if section = "style">
    <title>${kcSanitize(msg(emailAccountDataSubject!""))}</title>
  <#elseif section = "body">
    <@blocks.parameterizedMsg message=emailAccountCreateBodyHtml/>
  </#if>
</@template.layout>

<#--  TODO: Old logic  -->
<#--  <#import "template.ftl" as template>

<@template.layout ; section>
  <#if section = "style">
    <#include 'styles/content-style--default.html' >
    <title>${kcSanitize(msg("emailAccountDataSubject"))}</title>
  <#elseif section = "body">
    ${kcSanitize(msg(emailAccountCreateBodyHtml, userName, userFirstName, userLastName, accountLink))?no_esc}
  </#if>
</@template.layout>  -->
