<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
    <#elseif section = "body">
        <p>Срок действия пароля от учетной записи для входа в Личный кабинет «Дом.ру Бизнес» истёк. Перейдите по <a href="${link}">ссылке</a> для восстановления пароля.</p>
    </#if>
</@template.layout>