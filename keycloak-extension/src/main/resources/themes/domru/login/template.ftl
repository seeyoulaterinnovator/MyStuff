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
<#--        Убрал гугл аналитику, но оставил на будующие-->
<#--        <#if environment == "stage" || environment == "production" >-->
<#--            <#include "templates/google-tag-manager-head.html">-->
<#--        </#if>-->

        <#if properties.styles?has_content>
            <#list properties.styles?split(' ') as style>
                <link href="${url.resourcesPath}/${style}?hash=" rel="stylesheet"/>
            </#list>
        </#if>
    </head>
    <body class="min-h-full flex flex-col p-4 sm:px-6 md:py-6 lg:px-8 xl:py-8 xl:px-6">
    <#if displayMessage && message?has_content && message.summary == 'Регистрация временно недоступна, попробуйте повторить попытку позже'>
        <#if iframe == false>
            <@header.defaultTemplate withCity=displayCity></@header.defaultTemplate>
        </#if>
        <#include "templates/sth-went-wrong.html">
    <#else>
<#--        Убрал гугл аналитику, но оставил на будующие-->
<#--        <#if environment == "stage" || environment == "production" >-->
<#--            <#include "templates/google-tag-manager-body.html">-->
<#--        </#if>-->

        <#if iframe == false>
            <@header.defaultTemplate withCity=displayCity></@header.defaultTemplate>
        </#if>

        <main id="content" class="flex-1 py-8 md:py-12 mx-auto md:mx-auto w-full max-w-440px xl:max-w-470px">
            <#if displayMessage && message?has_content && message.summary == msg('emailSentMessage')>
                <@emailSent.defaultTemplate email="${login.username!}" backHref="${url.loginUrl}" success=true; section>
                    <#if section = "header">
                        Восстановление пароля
                    <#elseif section = "description">
                        <span>На почту: ${login.username!}</span><br>
                        Отправлены инструкции по восстановлению пароля
                    </#if>
                </@emailSent.defaultTemplate>
            <#elseif displayMessage && message?has_content && message.summary == msg('emailSendErrorMessage')>
                <@emailSent.defaultTemplate email="${login.username!}" backHref="${url.loginUrl}" success = false; section>
                    <#if section = "header">
                        Восстановление пароля
                    <#elseif section = "description">
                        Не получается отправить письмо. Учетная запись с такими данными не существует в системе.
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

        <#if iframe == false>
            <footer id="page-footer" class="w-full fixed bottom-0 footer">
                <a href="${(phoneConstLink)!"tel:88005500479"}" class= "show-small-tell">
                    <div class="flex h-6 items-center">
                        <svg width="19" height="20" viewBox="0 0 19 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M5.03341 4.12902C5.2853 3.87713 5.6937 3.87713 5.9456 4.12902L7.99182 6.17524C8.38346 6.56688 8.38346 7.20186 7.99182 7.5935C7.48302 8.1023 7.28745 8.84622 7.47635 9.53884C7.89974 11.0913 9.11969 12.3112 10.6721 12.7346C11.3648 12.9235 12.1087 12.728 12.6175 12.2192C13.0091 11.8275 13.6441 11.8275 14.0357 12.2192L15.3419 13.5253C15.6933 13.8768 15.6933 14.4466 15.3419 14.798C14.4689 15.6711 13.4536 16.2352 12.4491 16.4008C11.4571 16.5642 10.4516 16.3447 9.54609 15.6075C8.8175 15.0142 7.95941 14.2451 6.96264 13.2483C5.83397 12.1197 4.95756 11.1292 4.27924 10.2838C2.78739 8.42446 3.23894 5.92349 5.03341 4.12902Z" stroke="#222222"/>
                        </svg>
                        <span class="phone-number">${(phoneConst)!"8 800 550 0479"}</span>
                    </div>
                </a>
                <span class="text-main-500">${(footer)!"© АО «ЭР-Телеком Холдинг» 2011-"}
                <script>
                        document.write(new Date().getFullYear())
                </script>
            </span>
            </footer>
        </#if>

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

    <script type="text/javascript">

        let isFramed = false;
        try {
            isFramed = window !== window.top || document !== top.document || self.location !== top.location;
        } catch (e) {
            isFramed = true;
        }
        if (isFramed) {
            window.addEventListener('DOMContentLoaded', function (e) {
                if (document.getElementById('show-cities') != null) {
                    <#if withCity?has_content && withCity == "TRUE">
                    document.getElementById('show-cities').click()
                    document.getElementById('close-cities').style.display = 'none';
                    document.getElementById('cities-header-logo').style.display = 'none';
                    document.getElementById('cities-header-div').classList.replace("justify-between", "justify-center")
                    </#if>
                    document.getElementById('content').style.padding = '0';
                    document.getElementById('page-header').style.display = 'none';
                    document.getElementById('page-footer').style.display = 'none';
                    window.clearInterval(intervalID);
                }
            });
        }
    </script>
    <script>
        window.addEventListener('click', function (e) {
            if (!(e.target.classList.contains("allowDoubleClick"))){
                if (e.target.tagName.toLowerCase() == "button" || e.target.tagName.toLowerCase() == "a"){
                    e.target.style.pointerEvents = 'none';
                }
            }
        })
    </script>
    <!--<script src="${url.resourcesPath}/build/iframeResizer.contentWindow.min.js" async></script> -->
    </body>
    </html>
</#macro>
