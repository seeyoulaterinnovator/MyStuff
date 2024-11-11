<#import "template.ftl" as template>
<#import "blocks.ftl" as blocks>

<@template.layout ; section>
  <#if section="style">
    <#elseif section="body">
        ${kcSanitize(msg("emailResetPasswordBodyHtml", authHref, expTimePass))?no_esc}
        <@blocks.yourLogin login="${email!}" phone="${phone!}" />
        ${kcSanitize(msg("login"))?no_esc}
        <@blocks.recoveryPasswordInstruction />
  </#if>
</@template.layout>