<#import "template.ftl" as template>
<#import "blocks.ftl" as blocks>

<#--установить пароль при создании через админку-->
<@template.layout ; section>
  <#if section = "style">
  <#elseif section = "body">
    <div>
      <span style="font-size: 18px;   line-height: 24px;">
      Для вас создана учетная запись для входа в <a href="https://newlkb2b.dom.ru">Личный кабинет</a>
      </span>
      <div class="content__secondary content__secondary-mt">
        Срок действия ссылки ${expTimePass}.
      </div>
      <@blocks.yourLogin login="${email!}" phone="${phone!}"/>
      <div style="font-size: 18px;  margin-top: 16px">Пройдите по ссылке и <a href="${accountLink}">установите пароль</a></div>

  </#if>
</@template.layout>
