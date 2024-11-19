<#import "template.ftl" as template>
<#import "./helpers/blocks.ftl" as blocks>

<#--  Admin console - Users – Select user – Send login and reset password -->
<@template.layout ; section>
  <#if section="style">
    <title>${kcSanitize(msg(emailResetPasswordSubject!""))}</title>
  <#elseif section="body">
    <@blocks.parameterizedMsg message=emailResetPasswordBodyHtml/>
  </#if>
</@template.layout>