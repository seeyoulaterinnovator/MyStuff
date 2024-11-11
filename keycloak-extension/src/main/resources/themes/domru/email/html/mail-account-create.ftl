<#import "template.ftl" as template>
<#import "blocks.ftl" as blocks>
<#--import + default reg-->
<@template.layout ; section>
  <#if section = "style">
  <#elseif section = "body">
    <div>
      <span style="font-size: 18px; line-height: 24px;">
          Для вас создана учетная запись для входа в <a href="${accountLink}">Личный кабинет</a>
      </span>
      <div class="content__secondary content__secondary-mt">
        Срок действия ссылки ${expTimePass}.
      </div>
      <@blocks.yourLogin login="${email!}" phone="${phone!}"/>
      <div style="font-size: 18px;  line-height: 24px; font-weight: 400; margin-top: 16px">
        Благодарим вас за выбор услуг «Дом.ру Бизнес» для вашей компании!
      </div>
    </div>

    <#--${kcSanitize(msg(emailAccountCreateBodyHtml))?no_esc}-->
    <#--<#if phone??>
      ${kcSanitize(msg(emailLoginAndPhoneHtml, userName, phone))?no_esc}
    <#else>
      ${kcSanitize(msg(emailLoginHtml, userName))?no_esc}
    </#if>-->
  </#if>
</@template.layout>
