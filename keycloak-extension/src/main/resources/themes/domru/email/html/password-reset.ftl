<#import "template.ftl" as template>
<#--забыли пароль-->
<@template.layout ; section>
  <#if section = "style">
    <#include 'styles/custom-content-style.html' >
  <#elseif section = "body">

    <#assign email=realmName>
    <#if user?? && user.getEmail??>
      <#assign email= user.getEmail()>
    </#if>

    <#--${kcSanitize(msg(passwordResetBodyHtml,link, expTime, email, phone))?no_esc}-->
    <div style="font-family: Arial; color: #222;">
      <span style="font-size: 18px; line-height: 24px; ">
        Был создан запрос на изменение пароля от вашей учетной записи <a style="display: inline-block !important" href="${email}">${email}</a>. Если это были вы, пройдите по ссылке: <br>

        <div style="font-size: 18px;  margin-top: 16px"><a href="${link}">Изменить пароль</a></div>

      </span>

      <div style="font-size: 12px;   font-weight: 400; margin-top: 16px">
        Эта ссылка устареет через ${expTime}<br>
        Если вы не хотите сбрасывать пароль, просто проигнорируйте это письмо.
      </div>

    </div>
  </#if>
</@template.layout>