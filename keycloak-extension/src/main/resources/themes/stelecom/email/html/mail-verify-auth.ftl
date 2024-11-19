<#import "template.ftl" as template>
<#import "./helpers/blocks.ftl" as blocks>

<#--  Приходит из мобильного приложения  -->
<@template.layout ; section>
  <#if section="style">
    <title>${kcSanitize(msg(emailVerificationAuthSubject!""))}</title>
  <#elseif section="body">
    <@blocks.parameterizedMsg message=emailVerificationAuthBodyHtml />
  </#if>
</@template.layout>

<#--  TODO: Old logic  -->
<#--  <#import "template.ftl" as template>

<@template.layout ; section>
  <#if section = "style">
    <#include 'styles/content-style--default.html' >
    <title>${kcSanitize(msg("emailVerificationAuthSubject"))}</title>
  <#elseif section = "body">
    ${kcSanitize(msg("emailVerificationAuthBodyHtml",code))?no_esc}
  </#if>
</@template.layout>  -->
