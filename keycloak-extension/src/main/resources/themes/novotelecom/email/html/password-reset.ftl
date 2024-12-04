<#import "template.ftl" as template>
<#import "./helpers/blocks.ftl" as blocks>

<#-- ЛК - Забыли пароль? -->
<@template.layout ; section>
  <#if section="style">
    <title>${kcSanitize(msg(emailExpiresPasswordDataSubject!""))}</title>
  <#elseif section="body">
    <@blocks.parameterizedMsg message=passwordResetBodyHtml />
  </#if>
</@template.layout>