<#include "./image.ftl" />

<#macro code404 backHref backMessage="На главную" >
    <@defaultErrorTemplate heading="Похоже, такой страницы не существует" img="${url.resourcesPath}/build/images/404.png" backHref=backHref backMessage=backMessage >
        Провайдер домашнего интернета, телевидения и телефона в Новосибирске
        Электронный город. Подождите немного и попробуйте обновить
        страницу еще раз, или перейдите на главную страницу
    </@defaultErrorTemplate>
</#macro>

<#macro code500 backHref backMessage="Попробовать снова" >
    <@defaultErrorTemplate heading="Нет связи с сервером, но мы обязательно её восстановим" img="${url.resourcesPath}/build/images/500.png" backHref=backHref backMessage=backMessage >
        Провайдер домашнего интернета, телевидения и телефона в Новосибирске Электронный город.
        Подождите немного и попробуйте обновить страницу еще раз, или перейдите
    </@defaultErrorTemplate>
</#macro>

<#macro codeAll backHref backMessage="Попробовать снова" >
    <@defaultErrorTemplate heading="Что-то пошло не так" backHref=backHref img ="" backMessage=backMessage >
        Регистрация временно недоступна, попробуйте повторить попытку позже
    </@defaultErrorTemplate>
</#macro>

<#macro code403 backHref backMessage="Попробовать снова" >
    <@defaultErrorTemplate heading="Что-то пошло не так" backHref=backHref img ="" backMessage=backMessage >
        Ваша учетная запись заблокирована. Обратитесь в службу технической подджержки.
    </@defaultErrorTemplate>
</#macro>

<#macro code404_auth_or_reset backHref backMessage="Попробовать снова" >
    <@defaultErrorTemplate heading="Что-то пошло не так" backHref=backHref img ="" backMessage=backMessage >
        Ссылка устарела. Для совершения дальнейших действий необходимо авторизоваться или
        воспользоваться функцией 'Забыли пароль?'
    </@defaultErrorTemplate>
</#macro>

<#macro code404_tech_support backHref backMessage="Попробовать снова" >
    <@defaultErrorTemplate heading="Что-то пошло не так" backHref=backHref img ="" backMessage=backMessage >
        Ссылка устарела. Необходимо обратиться к специалисту технической поддержки для получения новой ссылки.
    </@defaultErrorTemplate>
</#macro>

<#macro code400 backHref iframe backMessage="Попробовать снова" >
    <#if iframe == "false" >
         <@defaultErrorTemplate heading="Что-то пошло не так" backHref=backHref img ="" backMessage=backMessage>
            Регистрация временно недоступна, попробуйте повторить попытку позже
        </@defaultErrorTemplate>
        <#else>
        <@defaultErrorTemplateIniframe heading="Что-то пошло не так" backHref=backHref img ="" backMessage=backMessage>
            Перезагрузите страницу
        </@defaultErrorTemplateIniframe>
    </#if>

</#macro>

<#macro defaultErrorTemplateIniframe heading backHref img backMessage >
    <#if img = "">
        <img class="error-background"
             src="${base64ErrorImg}"/>
    <#else>
        <img class="mb-8" src="${img}"/>
    </#if>
    <div class="page-title-wrapper">
        <h1 class="page-title error-heading">
            <b class="titleAllPage">${heading}</b>
        </h1>
    </div>
    <p class="text-sm">
        <#nested>
    </p>
</#macro>

<#macro defaultErrorTemplate heading backHref img backMessage >
    <div class="flex page-error-wrapper">
        <#if img = "">
            <img src="${base64ErrorImg}"/>
        <#else>
            <img class="mb-8" src="${img}"/>
        </#if>
        <div>
            <div class="page-title-wrapper custom-mb-sm">
                <h1 class="page-title">
                    <b class="titleAllPage">${heading}</b>
                </h1>
            </div>
            <p>
                <#nested>
            </p>
            <div class="page-buttons">
                <a class="btn btn-main swr-button swr-button-desktop" href="${backHref}">На главную</a>
                <a class="btn btn-main swr-button swr-button-tablet" href="${backHref}">Понятно</a>
            </div>
        </div>
    </div>
</#macro>
