<#import "template.ftl" as template>
<#import "./helpers/blocks.ftl" as blocks>

<#--  Admin console - Users – Select user – Send login -->
<@template.layout ; section>
  <#if section="style">
    <title>${kcSanitize(msg(emailSendLoginSubject!""))}</title>
  <#elseif section="body">
    <@blocks.parameterizedMsg message=emailSendLoginBodyHtml/>
  </#if>
</@template.layout>