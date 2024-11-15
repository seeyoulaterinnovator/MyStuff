<#import "template.ftl" as template>
<#import "./helpers/blocks.ftl" as blocks>

<#--  Admin console - Users – Select user – Block Users -->
<#--  Admin console - Users – Visit user's card – Enabled/Disabled -->
<@template.layout ; section>
  <#if section="style">
    <title>${kcSanitize(msg(emailDisabledAccountSubject!""))}</title>
  <#elseif section="body">
    <@blocks.parameterizedMsg message=emailDisabledAccountBodyHtml/>
  </#if>
</@template.layout>