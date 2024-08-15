<#import "template.ftl" as template>
<#--установить пароль при создании через админку-->
<@template.layout ; section>
  <#if section = "style">
  <#elseif section = "body">
    <div>
    <span style="font-size: 18px;   line-height: 24px;">
    Для вас создана учетная запись для входа в <a href="https://newlkb2b.dom.ru">Личный кабинет</a>
    </span>
      <div class="content__secondary content__secondary-mt">Срок действия ссылки ${expTimePass}.</div>
      <#if email??>
        <div class="content__secondary content__secondary-mt">Ваш логин:</div>
        <div style="font-size: 18px;   font-weight: 700">${email}</div>
      </#if>
      <#if phone ??>
        <div class="content__secondary">или</div>
        <div style="font-size: 18px;   font-weight: 700">${phone}</div>
      </#if>

      <div style="font-size: 18px;  margin-top: 16px">Пройдите по ссылке и <a href="${accountLink}">установите пароль</a></div>

    <#--${kcSanitize(msg(emailAccountDataBodyHtml))?no_esc}
    <#if phone??>
      ${kcSanitize(msg(emailLoginAndPhoneHtml, userName, phone))?no_esc}
    <#else>
      ${kcSanitize(msg(emailLoginHtml, userName))?no_esc}
    </#if>
    ${kcSanitize(msg(linkPassword, accountLink, expTimePass))?no_esc}-->
  </#if>
</@template.layout>
