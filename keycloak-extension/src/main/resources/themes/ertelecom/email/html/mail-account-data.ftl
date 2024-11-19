<#import "template.ftl" as template>
<#import "./helpers/blocks.ftl" as blocks>

<#-- ЛК - Регистрация -->
<@template.layout ; section>
  <#if section="style">
    <title>${kcSanitize(msg(emailAccountDataSubject!""))}</title>
  <#elseif section="body">
    <@blocks.parameterizedMsg message=emailAccountDataBodyHtml/>
  </#if>
</@template.layout>

<#--  TODO: Old logic  -->
<#--  <#import "template.ftl" as template>

<@template.layout ; section>
  <#if section = "style">
    <#include 'styles/content-style--default.html' >
    <title>${kcSanitize(msg("emailAccountDataSubject"))}</title>
  <#elseif section = "body">
    ${kcSanitize(msg(emailAccountDataBodyHtml, userName, userFirstName, userLastName, accountLink))?no_esc}
  </#if>
</@template.layout>  -->
