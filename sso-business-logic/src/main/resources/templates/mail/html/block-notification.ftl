<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
    <#elseif section = "body">
        <p>Ваша учетная запись для входа в Личный кабинет «Дом.ru Бизнес» заблокирована.</p>
        <p>Чтобы снять блокировку, обратитесь в службу технической поддержки «Дом.ru Бизнес» в чате на сайте <a href="https://newlkb2b.dom.ru">newlkb2b.dom.ru</a> или по телефону <a href="tel:88003339000">88003339000</a>.</p>
    </#if>
</@template.layout>