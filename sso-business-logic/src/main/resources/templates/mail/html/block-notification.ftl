<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
    <#elseif section = "body">
       <p>Ваша учетная запись для входа в Личный Кабинет "Дом.ru Бизнес" заблокирована. Для отмены блокировки Вам необходимо обратиться в службу технической поддержки Дом.ru Бизнес, используя чат на сайте <a href="https://b2b.domru.ru">https://b2b.domru.ru</a>, либо по номеру телефона <a href="tel:88003339000">88003339000</a>.</p>
    </#if>
</@template.layout>