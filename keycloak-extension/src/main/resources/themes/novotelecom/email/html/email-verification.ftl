<#import "template.ftl" as template>
<#import "./helpers/blocks.ftl" as blocks>

<#-- ЛК - Регистрация -->
<@template.layout ; section>
  <#if section="style">
    <title>${kcSanitize(msg(emailVerificationSubject!""))}</title>
  <#elseif section="body">
    <@blocks.parameterizedMsg message=emailVerificationBodyHtml/>
  </#if>
</@template.layout>