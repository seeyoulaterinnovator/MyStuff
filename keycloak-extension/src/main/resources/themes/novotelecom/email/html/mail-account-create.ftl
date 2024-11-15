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
