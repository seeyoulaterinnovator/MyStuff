<#import "template.ftl" as template>
<#import "./helpers/blocks.ftl" as blocks>

<#-- ЛК - Забыли пароль? -->
<@template.layout ; section>
  <#if section="style">
    <title>${kcSanitize(msg(emailExpiresPasswordDataSubject!""))}</title>
  <#elseif section="body">
    <@blocks.parameterizedMsg message=passwordResetBodyHtml />
  </#if>
</@template.layout>

<#--  TODO: Old logic  -->
<#--  <#import "template.ftl" as template>

<@template.layout ; section>
  <#if section = "style">
    <#include 'styles/content-style--default.html' >
    <title>${kcSanitize(msg("emailExpiresPasswordDataSubject"))}</title>
  <#elseif section = "body">

  <#assign email=realmName>
  <#if user?? && user.getEmail??>
    <#assign email= user.getEmail()>
  </#if>

    ${kcSanitize(msg("passwordResetBodyHtml",link, expTime, email, linkExpirationFormatter(linkExpiration)))?no_esc}
  </#if>
</@template.layout>  -->
