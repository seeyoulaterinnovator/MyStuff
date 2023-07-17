<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
    <#elseif section = "body">
        <p>Подтвердите вашу учетную запись для входа в Личный кабинет «Дом.ру Бизнес» по ссылке.
            Срок действия ссылки ${expTime}.</p>
        <p class = "block_link"><a href="${link}">Подтвердить учетную запись</a></p>
    </#if>
</@template.layout>

