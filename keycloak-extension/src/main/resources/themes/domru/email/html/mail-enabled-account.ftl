<#import "template.ftl" as template>
<#import "blocks.ftl" as blocks>

<@template.layout ; section>
  <#if section = "style">
  <#elseif section = "body">
    <div>
      <span style="font-size: 18px;   line-height: 24px;">
        Ваша учетная запись для входа в <a style="display: inline-block !important" href="https://newlkb2b.dom.ru">Личный кабинет</a> «Дом.ру Бизнес» <span style="color: #15A250"> разблокирована.</span>
	  </span>
      <@blocks.yourLogin login="${email!}" />
      ${kcSanitize(msg("login"))?no_esc}

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
