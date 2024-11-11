<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
    <#elseif section = "body">
        <p>Подтвердите вашу учетную запись для входа в Личный кабинет<br/>«Дом.ру Бизнес».
            Срок действия ссылки ${expTime}.</p>
        <p class="block_link"><a href="${link}">Подтвердить учетную запись</a></p>
    </#if>
</@template.layout>

