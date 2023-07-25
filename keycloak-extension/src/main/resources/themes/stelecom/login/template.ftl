<#import "templates/email-sent.ftl" as emailSent>
<#import "templates/header.ftl" as header>

<#macro registrationLayout displayInfo=false displayMessage=true displayWarningMessage=true displayWide=false environment="production" displayCity=true>
    <!DOCTYPE html>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="ru" class="h-full scrollable-container">
    <head>
        <meta charset="utf-8">
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="robots" content="noindex, nofollow">
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
        <link rel="shortcut icon" href="${url.resourcesPath}/build/images/favicon.ico" type="image/x-icon">

        <#if properties.meta?has_content>
            <#list properties.meta?split(' ') as meta>
                <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
            </#list>
        </#if>
        <title>${msg("loginTitle",(realm.displayName!''))}</title>

        <#if environment == "stage" || environment == "production" >
            <#include "templates/google-tag-manager-head.html">
        </#if>

        <#if properties.styles?has_content>
            <#list properties.styles?split(' ') as style>
                <link href="${url.resourcesPath}/${style}?hash=" rel="stylesheet"/>
            </#list>
        </#if>
    </head>
    <body class="min-h-full flex flex-col p-4 sm:px-6 md:py-6 lg:px-8 xl:py-8 xl:px-6">
    <#if displayMessage && message?has_content && message.summary == 'Регистрация временно недоступна, попробуйте повторить попытку позже'>
        <@header.defaultTemplate withCity=displayCity></@header.defaultTemplate>
        <#include "templates/sth-went-wrong.html">
    <#else>
        <#if environment == "stage" || environment == "production" >
            <#include "templates/google-tag-manager-body.html">
        </#if>

        <@header.defaultTemplate withCity=displayCity></@header.defaultTemplate>

        <main id="content" class="flex-1 py-8 md:py-12 mx-auto md:mx-auto w-full max-w-440px xl:max-w-470px">
            <#if displayMessage && message?has_content && message.summary == msg('emailSentMessage')>
                <@emailSent.defaultTemplate email="${login.username!}" backHref="${url.loginUrl}"; section>
                    <#if section = "header">
                        Восстановление пароля
                    <#elseif section = "description">
                        <span>На почту: ${login.username!}</span>
                        Отправлены инструкции для восстановления пароля
                    </#if>
                </@emailSent.defaultTemplate>
            <#else>

                <#nested "header">

                <#if displayInfo>
                    <#nested "info">
                </#if>

                <div class="py-2 sm:py-3 lg:py-4">
                    <#if displayMessage && message?has_content>
                        <div class="alert pb-4">
                            <#if message.type = 'info'>
                                <span class="text-black">
                                    ${kcSanitize(message.summary)?no_esc}
                                </span>
                            </#if>
                            <#if message.type = 'warning' && displayWarningMessage>
                                <span class="text-black">
                                    ${kcSanitize(message.summary)?no_esc}
                                </span>
                            </#if>
                            <#if message.type = 'success' && message.summary != msg('emailSentMessage')>
                                <span class="text-accentGreen">
                                    ${kcSanitize(message.summary)?no_esc}
                                </span>
                            </#if>
                            <#if message.type = 'error'>
                                <#if message.summary?contains('Номер мобильного телефона уже используется в другой учетной записи.')>
                                    <#if message.summary?contains(msg('emailExistsMessage'))>
                                        <span class="text-accentRed bad_phone bad_email hidden">
                                            ${kcSanitize(message.summary)?no_esc}
                                        </span>
                                    <#else>
                                        <span class="text-accentRed bad_phone hidden">
                                            ${kcSanitize(message.summary)?no_esc}
                                        </span>
                                    </#if>
                                <#elseif message.summary?contains('Превышен лимит СМС. Запросить новое СМС можно через 5 минут')>
                                    <span class="text-accentRed hidden limit-exceeded">
                                        ${kcSanitize(message.summary)?no_esc}
                                    </span>
                                <#elseif message.summary?contains('Превышен лимит повторных звонков. Запросить новый звонок можно через 12 часов')>
                                    <span class="text-accentRed hidden limit-exceeded">
                                        ${kcSanitize(message.summary)?no_esc}
                                    </span>
                                <#elseif message.summary?contains('Код был введён более 5 раз. Запросите новое СМС')>
                                    <span class="text-accentRed hidden limit-exceeded">
                                        ${kcSanitize(message.summary)?no_esc}
                                    </span>
                                <#elseif message.summary?contains('Код был введён более 5 раз. Запросите новый звонок')>
                                    <span class="text-accentRed hidden limit-exceeded">
                                        ${kcSanitize(message.summary)?no_esc}
                                    </span>
                                <#elseif message.summary?contains('Код введен неверно. Проверьте правильность введенных данных')>
                                    <span class="text-accentRed hidden">
                                        ${kcSanitize(message.summary)?no_esc}
                                    </span>
                                <#elseif message.summary == msg('emailExistsMessage')>
                                    <span class="text-accentRed bad_email hidden">
                                        ${kcSanitize(message.summary)?no_esc}
                                    </span>
                                <#else>
                                    <span class="text-accentRed hidden">
                                        ${kcSanitize(message.summary)?no_esc}
                                    </span>
                                </#if>
                            </#if>
                        </div>
                    </#if>

                    <#nested "form">

                </div>

            </#if>
        </main>

        <#include "templates/footer-copyright.html">

        <div id="cities-modal"></div>
        <div id="message-modal" data-login-url="${url.loginRestartFlowUrl}"></div>
    </#if>

    <#if properties.scripts?has_content>
        <#list properties.scripts?split(' ') as script>
            <script src="${url.resourcesPath}/${script}?hash=" async></script>
        </#list>
    </#if>

    <#if scripts??>
        <#list scripts as script>
            <#if script?contains("recaptcha/api.js?")>
                <script src="${script}&onload=onloadRecaptchaCallback" async defer></script>
            <#elseif script?contains("recaptcha/api.js")>
                <script src="${script}?onload=onloadRecaptchaCallback" async defer></script>
            <#else>
                <script src="${script}" async defer></script>
            </#if>
        </#list>
    </#if>

    <#if hideChat>
        <div id="hiddenChat" class="hidden">
        </div>
    </#if>

    <script type="text/javascript">
        let isChatHidden = document.getElementById('hiddenChat');

        let isFramed = false;
        try {
            isFramed = window !== window.top || document !== top.document || self.location !== top.location;
        } catch (e) {
            isFramed = true;
        }
        if (isFramed) {
            var x = 0;
            var intervalID = setInterval(function () {
                if (document.getElementById('show-cities') != null) {
                    <#if withCity?has_content && withCity == "TRUE">
                    document.getElementById('show-cities').click()
                    document.getElementById('close-cities').style.display = 'none';
                    document.getElementById('cities-header-logo').style.display = 'none';
                    document.getElementById('cities-header-div').classList.replace("justify-between", "justify-center")
                    </#if>
                    document.getElementById('page-header').style.display = 'none';
                    document.getElementById('page-footer').style.display = 'none';
                    document.getElementById('content').style.padding = '0';
                    window.clearInterval(intervalID);
                } else if (++x > 50) {
                    window.clearInterval(intervalID);
                }
            }, 150);
        }
    </script>
    <!--<script src="${url.resourcesPath}/build/iframeResizer.contentWindow.min.js" async></script> -->
    </body>
    </html>
</#macro>
