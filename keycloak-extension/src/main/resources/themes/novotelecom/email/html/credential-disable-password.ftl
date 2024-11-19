<#import "template.ftl" as template>
<#import "./helpers/blocks.ftl" as blocks>

<#-- Admin console - Users - Visit user's card - Credentials - Delete password -->
<@template.layout ; section>
  <#if section = "style">
    <title>${kcSanitize(msg(emailCredentialDisableSubject!""))}</title>
  <#elseif section = "body">
    <@blocks.parameterizedMsg message=emailCredentialDisableBodyHtml/>
  </#if>
</@template.layout>
