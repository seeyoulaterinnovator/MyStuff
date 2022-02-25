<#import "template.ftl" as layout>
<#import "templates/error.ftl" as errorPage>

<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        <#--  ${msg("errorTitle")}  -->
    <#elseif section = "form">
        <#if client?? && client.baseUrl?has_content>
            <#assign backHref="${client.baseUrl}" backMessage="${kcSanitize(msg('backToApplication'))?no_esc}" >
        </#if>

        <#if message.summary?contains('Page not found') || message.summary?contains('Неверный параметр')  >
            <@errorPage.code404 backHref="${url.loginUrl}" backMessage=backMessage />
        <#elseif message.summary?contains('500') >
            <@errorPage.code500 backHref="${url.loginUrl}" backMessage=backMessage />
        <#elseif message.summary?contains('Учетная запись временно заблокирована, свяжитесь с администратором или попробуйте позже.')>
            <@errorPage.code403 backHref="${redirectUrl}" backMessage=backMessage />
        <#elseif message.summary?contains('Время истекло. Продолжить авторизацию.')>
            <@errorPage.code40401 backHref="${redirectUrl}" backMessage=backMessage />
        <#elseif message.summary?contains('Ссылка устарела. Для совершения дальнейших действий необходимо авторизоваться или воспользоваться функцией "Забыли пароль?"')>
            <@errorPage.code40402 backHref="${redirectUrl}" backMessage=backMessage />
        <#else>
            <@errorPage.codeAll backHref="${url.loginUrl}" backMessage=backMessage />
        </#if>

    </#if>
</@layout.registrationLayout>