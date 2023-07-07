<#import "template.ftl" as template>

<@template.layout ; section>
  <#if section = "style">
    <#include 'styles/custom-content-style.html' >
  <#elseif section = "body">
    <div style=" color: #222; font-family: Arial;">
      <span style="font-size: 18px;   line-height: 24px;">
        Ваша учетная запись для входа в <a style="display: inline-block !important" href="https://newlkb2b.dom.ru">Личный кабинет</a> «Дом.ру Бизнес» <span style="color: #15A250"> разблокирована.</span>
	  </span>

      <div style="font-size: 12px;   font-weight: 400; margin-top: 16px">Ваш логин:</div>
      <div style="font-size: 18px;   font-weight: 700">${email}</div>

      <div style="font-size: 18px;  margin-top: 16px"><a href="https://newlkb2b.dom.ru">Перейти в
          Личный кабинет</a></div>
    </div>
    <#--${kcSanitize(msg(emailEnabledAccountBodyHtml, email))?no_esc}-->
    <#--<#if phone??>
      ${kcSanitize(msg(emailLoginAndPhoneHtml, userName, phone))?no_esc}
    <#else>
      ${kcSanitize(msg(emailLoginHtml, userName))?no_esc}
    </#if>-->
    <#--${kcSanitize(msg("login"))?no_esc}-->
  </#if>
</@template.layout>
