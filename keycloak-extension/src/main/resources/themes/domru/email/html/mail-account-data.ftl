<#import "template.ftl" as template>

<@template.layout ; section>
  <#if section = "style">
    <#include 'styles/content-style--default.html' >
  <#elseif section = "body">
    ${kcSanitize(msg("emailAccountDataBodyHtml", userName, userFirstName, userLastName, password, accountLink))?no_esc}
  </#if>
</@template.layout>
