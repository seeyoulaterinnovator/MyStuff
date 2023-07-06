<#import "template.ftl" as template>

<@template.layout ; section>
  <#if section = "style">
    <#include 'styles/custom-content-style.html' >
  <#elseif section = "body">
    ${kcSanitize(msg(emailVerificationAuthBodyHtml,code))?no_esc}
  </#if>
</@template.layout>
