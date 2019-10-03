<#import "template.ftl" as layout>
<#import "templates/email-sent.ftl" as emailSent>

<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
    <#elseif section = "form">
        <#assign email=realm.displayName>
        <#if brokerContext?? && brokerContext.email??>
            <#assign email= brokerContext.email>
        </#if>
        <@emailSent.defaultTemplate email="${email!}" buttonExist=false; section>
            <#if section = "header">
              Подтверждение данных
            <#elseif section = "description">
              Отправлены инструкции для связывания аккаунтов
            </#if>
        </@emailSent.defaultTemplate>
    </#if>
</@layout.registrationLayout>