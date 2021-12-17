<#import "template.ftl" as template>

<@template.layout ; section>
  <#if section = "style">
    <#include 'styles/content-style--default.html' >
  <#elseif section = "body">
    ${kcSanitize(msg("emailAccountCreateBodyHtml", userName, userFirstName, userLastName, accountLink))?no_esc}
  </#if>
</@template.layout>
