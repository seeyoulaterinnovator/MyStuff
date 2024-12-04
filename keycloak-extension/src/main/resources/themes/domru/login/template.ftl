<#import "templates/email-sent.ftl" as emailSent>
<#import "templates/header.ftl" as header>
<#import "./templates/svg.ftl" as svg>

<#macro registrationLayout
        displayInfo=false
        displayMessage=true
        displayWarningMessage=true
        displayWide=false
        environment="production"
        displayCity=true
        redirectTo=""
        redirectToOnModalClose=""
        bodyClass=""
>
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
<#--        Убрал гугл аналитику, но оставил может еще понадобиться-->
<#--        <#if environment == "stage" || environment == "production" >-->
<#--            <#include "templates/google-tag-manager-head.html">-->
<#--        </#if>-->

        <#if properties.styles?has_content>
            <#list properties.styles?split(' ') as style>
                <link href="${url.resourcesPath}/${style}?hash=@hash@" rel="stylesheet"/>
            </#list>
        </#if>
        <#if redirectTo?has_content>
            <script>
              document.location = "${redirectTo}".replaceAll('&amp;', '&');
            </script>
        </#if>

    </head>
    <body class="min-h-full flex flex-col custom main-wrapper">
    <#if displayMessage?has_content && displayMessage && message?has_content && message.summary == 'Регистрация временно недоступна, попробуйте повторить попытку позже'>
        <#if iframe ?? && iframe == false>
            <@header.defaultTemplate withCity=displayCity></@header.defaultTemplate>
        </#if>
        <#include "templates/sth-went-wrong.html">
    <#else>
<#--        Убрал гугл аналитику, но оставил может еще понадобиться-->
<#--        <#if environment == "stage" || environment == "production" >-->
<#--            <#include "templates/google-tag-manager-body.html">-->
<#--        </#if>-->

        <#if iframe ?? && iframe == false>
            <@header.defaultTemplate withCity=displayCity></@header.defaultTemplate>
        </#if>

        <main id="content" class="flex-1 py-8 md:py-12 mx-auto md:mx-auto w-full" style="overflow: initial;">


            <#if displayMessage?has_content && displayMessage && message?has_content && message.summary == msg('emailSentMessage')>
                <@emailSent.defaultTemplate email="${userEmail!}" backHref="${url.loginUrl}" success=true; section>
                    <#if section = "header">
                        Восстановление пароля
                    <#elseif section = "description">
                        <span>На e-mail: ${userEmail!"указанную при регистрации"}</span><br>
                        Отправлены инструкции по восстановлению пароля
                    </#if>
                </@emailSent.defaultTemplate>

            <#elseif displayMessage?has_content && displayMessage && message?has_content && message.summary == msg('emailSendErrorMessage')>
                <@emailSent.defaultTemplate email="${userEmail!}" backHref="${url.loginUrl}" success = false; section>
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

                <div class="">
                    <#if displayMessage && message?has_content>
                        <div class="alert">
                            <#if message.type = 'info'>
                            <#if message.summary?contains('Ваш E-mail успешно подтверждён!')>
                                <p class="text-black email-ver hidden">
                                        ${kcSanitize(message.summary)?no_esc}
                                </p>
                            <#else>
                                <p class="text-black hidden">
                                    ${kcSanitize(message.summary)?no_esc}
                                </p>
                            </#if>
                            </#if>
                            <#if message.type = 'warning' && displayWarningMessage>
                                <p class="text-black">
                                    ${kcSanitize(message.summary)?no_esc}
                                </p>
                            </#if>
                            <#if message.type = 'success' && message.summary != msg('emailSentMessage')>
                                <p class="text-accentGreen">
                                    ${kcSanitize(message.summary)?no_esc}
                                </p>
                            </#if>
                            <#if message.type = 'error'>
                                <#if loginFailToRegistrationMessage?has_content>
                                    <p class="text-accentRed login-fail-to-registration hidden">
                                        ${kcSanitize(loginFailToRegistrationMessage)?no_esc}
                                    </p>
                                <#elseif message.summary?contains('Номер мобильного телефона уже используется в другой учетной записи.')>
                                    <#if message.summary?contains(msg('emailExistsMessage'))>
                                        <p class="text-accentRed bad_phone bad_email hidden">
                                            ${kcSanitize(message.summary)?no_esc}
                                        </p>
                                    <#else>
                                        <p class="text-accentRed bad_phone hidden">
                                            ${kcSanitize(message.summary)?no_esc}
                                        </p>
                                    </#if>
                                <#elseif message.summary?contains('Превышен лимит СМС. Запросить новое СМС можно через 5 минут')>
                                    <p class="text-accentRed hidden limit-exceeded">
                                        ${kcSanitize(message.summary)?no_esc}
                                    </p>
                                <#elseif message.summary?contains('Превышен лимит повторных звонков. Запросить новый звонок можно через 12 часов')>
                                    <p class="text-accentRed hidden limit-exceeded">
                                        ${kcSanitize(message.summary)?no_esc}
                                    </p>
                                <#elseif message.summary?contains('Код был введён более 5 раз. Запросите новое СМС')>
                                    <p class="text-accentRed hidden limit-exceeded">
                                        ${kcSanitize(message.summary)?no_esc}
                                    </p>
                                <#elseif message.summary?contains('Код был введён более 5 раз. Запросите новый звонок')>
                                    <p class="text-accentRed hidden limit-exceeded">
                                        ${kcSanitize(message.summary)?no_esc}
                                    </p>
                                <#elseif message.summary == msg('emailExistsMessage')>
                                    <p class="text-accentRed bad_email hidden">
                                        ${kcSanitize(message.summary)?no_esc}
                                    </p>
                                <#elseif message.summary?contains('Код введен неверно. Проверьте правильность введенных данных')>
                                    <p class="text-accentRed hidden">
                                        ${kcSanitize(message.summary)?no_esc}
                                    </p>
                                <#elseif message.summary == msg('Авторизация с использованием временного кода в данный момент не доступна. Для авторизации воспользуйтесь логином и паролем')>
                                    <p class="text-accentRed phone_error hidden">
                                        ${kcSanitize(message.summary)?no_esc}
                                    </p>
                                <#else>
                                    <p class="text-accentRed hidden">
                                        ${kcSanitize(message.summary)?no_esc}
                                    </p>
                                </#if>
                            </#if>
                        </div>
                    </#if>

                    <#nested "form">

                </div>

            </#if>
        </main>

        <#if iframe?? && iframe == false>
            <footer id="page-footer" class="w-full flex flex-col footer">
              <a href="${(phoneConstLink)!"tel:88005500479"}" class="show-small-tell highlighted-hover-text highlighted-nested-hover-svg phone-call-center">
                <div class="flex h-6 items-center">
                  <@svg.phoneIcon/>
                  <span class="phone-number">
                    ${(phoneConst)!"8 800 550 0479"}
                  </span>
                </div>
              </a>
              <span class="text-main-500 copyright mt-2">
                ${(footer)!"© АО «ЭР-Телеком Холдинг» 2011–"}${.now?string('yyyy')}
              </span>
            </footer>
        </#if>

        <div id="cities-modal"></div>
        <div id="message-modal" data-login-url="${url.loginUrl}" data-registration-url="${url.registrationUrl}"></div>
    </#if>

    <#if properties.scripts?has_content>
        <#list properties.scripts?split(' ') as script>
            <script src="${url.resourcesPath}/${script}?hash=@hash@" async></script>
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

    <#if hideChat?? && hideChat == false>
        <div id="hiddenChat" class="hidden">
        </div>
    </#if>

    <script type="text/javascript">
        let chatElement = document.getElementById('hiddenChat');

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
    <#--
    <#include "util.ftl"/>
    <@dump_data_model_keys/>
    -->
</#macro>
