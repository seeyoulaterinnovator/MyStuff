<#import "template.ftl" as layout>
<#import "templates/email-sent.ftl" as emailSent>

<@layout.registrationLayout displayMessage=false displayCity=false; section>
    <#if section = "form">
        <@emailSent.defaultTemplate email="{mail!}" backHref="/" buttonExist=true; section>
            <#if section = "header">
                Подтверждение данных
            <#elseif section = "description">
                На почту: ${mail!} <br/>
                Отправлены инструкции для авторизации
            </#if>
        </@emailSent.defaultTemplate>
    </#if>
</@layout.registrationLayout>
