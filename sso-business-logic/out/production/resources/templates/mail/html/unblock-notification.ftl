<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
    <#elseif section = "body">
      <p>Ваша учетная запись для входа в Личный кабинет «Дом.ru Бизнес» разблокирована.</p>
    </#if>
</@template.layout>