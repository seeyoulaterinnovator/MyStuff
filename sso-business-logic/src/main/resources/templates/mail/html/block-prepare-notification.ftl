<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
    <#elseif section = "body">
        <p>Ваша учетная запись для входа в Личный Кабинет «Дом.ru Бизнес» будет заблокирована через ${absence} дней.</p>
        <p>Для предотвращения блокировки УЗ, просим вас войти в Личный кабинет "Дом.ru Бизнес" до истечения указанного срока.</p>
    </#if>
</@template.layout>