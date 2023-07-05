<#import "template.ftl" as template>

<@template.layout ; section>
  <#if section = "style">
    <#include 'styles/custom-content-style.html' >
  <#elseif section = "body">

    <#assign email=realmName>
    <#if user?? && user.getEmail??>
      <#assign email= user.getEmail()>
    </#if>

    ${kcSanitize(msg(passwordResetBodyHtml,link, expTime, email, phone))?no_esc}
  </#if>
</@template.layout>