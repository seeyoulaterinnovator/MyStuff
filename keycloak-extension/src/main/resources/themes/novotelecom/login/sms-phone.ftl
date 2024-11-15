<#import "template.ftl" as layout>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayMessage=true displayCity=false; section>
    <#if section = "header">
        <@blocks.contentHeader mainTitle="Вход" secondaryTitle="" secondaryHref="" withBorder=true />

    <#elseif section = "form">
        <#if phoneCallButton!false>
            <p class="verification__sub custom-mb-md">
                Введите последние 4 цифры входящего номера
            </p>
        <#else>
            <p class="verification__sub custom-mb-md">
                Введите код из СМС
            </p>
        </#if>
        <form id="totpe" action="${url.loginAction}" method="POST"></form>

        <form id="totpForm" action="${url.loginAction}" method="POST">

            <div class="sm:block md:flex w-full items-center text-center md:text-left">
                <button class="hidden"
                        name="accept" id="accept" type="submit">${doSubmit}</button>

            </div>

            <div class="w-full center-items sms-inputs">
                <#list 1..lengthCode as x>
                    <input type="text" inputmode="numeric" pattern="[0-9]*" placeholder="-" maxlength="1" id="smscode-${x}"
                           name="smscode-${x} " autocomplete="one-time-code"
                           <#if isMoreThanFiveAttempts?? && isMoreThanFiveAttempts>
                               disabled
                           <#elseif codeLimited?? && codeLimited>
                               disabled
                           </#if>
                           class="text-center align-middle w-14 h-14 border rounded-lg focus:border-extra outline-none squares
                            field__input sms-input" x == 1 && autofocus/>
                </#list>
            </div>

            <input id="codeNumbers" name="codeNumbers" class="hidden" value="${lengthCode!}"/>
            <input id="expirationSeconds" name="expirationSeconds" class="hidden" value="${expirationSeconds!}"/>
            <input id="smscode" name="smscode" class="hidden"/>

            <div class="back-timer lex justify-between">
                <#if isMoreThanFiveAttempts?? && isMoreThanFiveAttempts>
                <span>
                            <button class="font-light verification__resend w-full" name="resend" type="submit">${sendAgain}</button>
                        </span>
                <#else>
                <div class="flex justify-between enter-login-link-timer enter-login-link-resend page-buttons w-full">
                    <a id="topSecretButton" class="resend highlighted-hover-text" href="#"
                       onclick="document.getElementById('loginPasswordButton').click();">Войти с помощью логина
                    </a>
                    <div id="timer" class="ml-auto timer">
                        <span class="timer-new">
                            Код действует: 
                        </span>
                        <span> </span>
                        <span id="timer-time" class="timer-new-countdown"></span>
                    </div>
                    <#if enableRepeatCall?? && enableRepeatCall!>
                        <p class="hidden font-light text-black verification__text" id="resend">
                                <span>
                                    <button class="resend font-light highlighted-hover-text" name="resend"
                                            type="submit">${sendAgain}</button>
                                </span>
                        </p>
                    </#if>
                </div>
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
