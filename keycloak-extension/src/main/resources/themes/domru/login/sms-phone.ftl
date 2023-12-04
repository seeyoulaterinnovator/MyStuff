<#import "template.ftl" as layout>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayMessage=true displayCity=false; section>
    <#if section = "header">
        <@blocks.contentHeader mainTitle="Вход" secondaryTitle="Регистрация" secondaryHref="${url.registrationUrl}" withBorder=true />

    <#elseif section = "form">
        <#if phoneCallButton!false>
            <h3 class="verification__sub pb-2 sm:pb-3 md:pb-4 info-text info-text-code">
                Введите последние 4 цифры входящего номера
            </h3>
        <#else>
            <h3 class="verification__sub pb-2 sm:pb-3 md:pb-4 info-text info-text-code">
                Введите код из СМС
            </h3>
        </#if>
        <form id="totpe" action="${url.loginAction}" method="POST"></form>

        <form id="totpForm" action="${url.loginAction}" method="POST">

            <div class="sm:block md:flex w-full items-center text-center md:text-left">
                <button class="hidden"
                        name="accept" id="accept" type="submit">${doSubmit}</button>

            </div>

            <div class="w-full xl:pb-37px md:pb-10 sm:pb-8 pb-4 center-items">
                <#list 1..lengthCode as x>
                    <input placeholder="-" maxlength="1" id="smscode-${x}" style="font-size: 22px;"
                           name="smscode-${x} " autocomplete="one-time-code"
                           <#if isMoreThanFiveAttempts?? && isMoreThanFiveAttempts>
                               disabled
                           <#elseif codeLimited?? && codeLimited>
                               disabled
                           </#if>
                           class="text-center align-middle w-14 h-14 border rounded-lg focus:border-extra outline-none squares
                            sms-input" x == 1 && autofocus/>
                </#list>
            </div>

            <input id="codeNumbers" name="codeNumbers" class="hidden" value="${lengthCode!}"/>
            <input id="expirationSeconds" name="expirationSeconds" class="hidden" value="${expirationSeconds!}"/>
            <input id="smscode" name="smscode" class="hidden"/>

            <div class="back-timer lex justify-between">
<#--                <div>-->
<#--                    <button id="topSecretButton" type="button" class="btn btn-back text-accentBlue-900"-->
<#--                            onclick="document.getElementById('loginPasswordButton').click();">Войти с помощью логина-->
<#--                    </button>-->
<#--                    <a id="topSecretButton" class="enter-login-link" href="#"-->
<#--                       onclick="document.getElementById('loginPasswordButton').click();">Войти с помощью логина-->
<#--                    </a>-->
<#--                </div>-->
                <#if isMoreThanFiveAttempts?? && isMoreThanFiveAttempts>
                        <span>
                            <button class="font-light verification__resend w-full" name="resend" type="submit">${sendAgain}</button>
                        </span>
                <#else>
                <div class="flex justify-between enter-login-link-timer enter-login-link-resend ">
                    <a id="topSecretButton" class="resend" href="#"
                       onclick="document.getElementById('loginPasswordButton').click();">Войти с помощью логина
                    </a>
                    <div id="timer"  style="margin-left: auto;">
                            <span class="timer-new">
                                Код действует:
                            </span>
                        <span id="timer-time" class="timer-new-countdown"></span>
                    </div>
                </div>
                        <#if enableRepeatCall?? && enableRepeatCall!>
                            <p class="hidden font-light text-black verification__text" id="resend">
                                <span>
                                    <button class="font-light verification__resend resend" name="resend"
                                                type="submit">${sendAgain}</button>
                                </span>
                            </p>
                        </#if>
                    </div>
                </#if>

        </form>
        <form method="POST" action="${url.loginUrl}">
            <button id="loginPasswordButton" name="back" type="submit" class="hidden">
                Войти с помощью логина
            </button>
        </form>
    </#if>
    <script>
        var actionIsEmpty = ${actionIsEmpty?c};
        var clientIsB2B = ${clientIsB2B?c};

        if (clientIsB2B === true && actionIsEmpty === true) {
            window.onunload = function () {
                window.parent.postMessage('post-selected', '*');
                console.log("Отправлено тк B2B и Action пуст");
            };
        }

        if ('OTPCredential' in window) {
            window.addEventListener('DOMContentLoaded', e => {
                const inputs = document.querySelectorAll('input[autocomplete="one-time-code"]');
                if (!inputs.length) return;
                const ac = new AbortController();
                const form = inputs[0].closest('form');
                if (form) {
                    form.addEventListener('submit', e => {
                        ac.abort();
                    });
                }
                navigator.credentials.get({
                    otp: { transport:['sms'] },
                    signal: ac.signal
                }).then(otp => {
                    let numbers = otp.code.split('');
                    [...inputs].forEach((it, idx) => it.value = numbers[idx]);
                    let code = document.getElementById('smscode')
                    code.value = otp.code;
                    if (form)
                        form.submit();
                }).catch(err => {
                    console.log(err);
                });
            });
        }

    </script>
</@layout.registrationLayout>
