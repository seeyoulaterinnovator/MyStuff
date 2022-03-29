<#import "template.ftl" as template>

<@template.layout ; section>
  <#if section = "style">
    <#include 'styles/content-style--default.html' >
  <#elseif section = "body">
    ${kcSanitize(msg(emailAccountDataBodyHtml))?no_esc}
    <#if phone??>
      ${kcSanitize(msg(emailLoginAndPhoneHtml, userName, phone))?no_esc}
    <#else>
      ${kcSanitize(msg(emailLoginHtml, userName))?no_esc}
    </#if>
    ${kcSanitize(msg(linkPassword, accountLink, expTimePass))?no_esc}
  </#if>
</@template.layout>
