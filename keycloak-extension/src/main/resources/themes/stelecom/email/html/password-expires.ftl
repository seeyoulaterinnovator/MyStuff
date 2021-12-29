<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
    <#elseif section = "body">
        <p>Срок действия пароля от учетной записи для входа в Личный кабинет «С-Телеком Бизнес» истёк. Для восстановления пароля Вам необходимо перейти по <a href="${link}">ссылке</a> и воспользоваться функцией «Забыли пароль?»</p>
    </#if>
</@template.layout>
