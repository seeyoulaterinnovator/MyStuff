<#import "template.ftl" as layout>
<#import "templates/email-sent.ftl" as emailSent>

<@layout.registrationLayout  displayMessage=false displayCity=false; section>
    <#if section = "form">
    <@emailSent.defaultTemplate email="${mail!}" buttonExist=false; section>
        <#if section = "header">
        Подтверждение данных
        <#elseif section = "description">
        Отправлены инструкции для авторизации
        </#if>
    </@emailSent.defaultTemplate>
    </#if>
    
</@layout.registrationLayout>