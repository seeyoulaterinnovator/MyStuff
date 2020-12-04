<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
    <#elseif section = "body">
        <p>Ваша учетная запись для входа в Личный кабинет «Дом.ru Бизнес» будет заблокирована через ${absence}.</p>
        <p>Для предотвращения блокировки учетной записи, просим Вас войти в <a href="${link}">Личный кабинет</a> до истечения указанного срока.</p>
    </#if>
</@template.layout>