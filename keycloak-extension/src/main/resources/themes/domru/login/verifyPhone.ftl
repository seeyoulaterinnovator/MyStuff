<#import "template.ftl" as layout>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayMessage=true displayCity=false; section>
    <#if section = "header">
        <#if isRegistration>
            <@blocks.contentHeader mainTitle="Регистрация" secondaryTitle="Вход" secondaryHref="${url.loginUrl}" withBorder=true />
        <#else>
            <@blocks.verificationHeader mainTitle="Подтвердить" />
        </#if>
    <#elseif section = "form">
        <#if activationCodeType == "CODE_TO_SMS">
            <h3 class="verification__sub">
                Введите код из СМС отправленный на указанный номер телефона
            </h3>
        </#if>
        <#if activationCodeType == "CODE_BY_PHONE_NUMBER">
            <h3 class="verification__sub" x-ms-format-detection="none">
                На указанный номер поступит звонок. Для подтверждения нужно ввести последние 4 цифры входящего номера
            </h3>
        </#if>
        <form id="totpe" action="${url.loginAction}" method="POST"></form>

        <form id="totpForm" action="${url.loginAction}" method="POST">

            <div class="sm:block md:flex w-full items-center text-center md:text-left">
                <button class="btn btn-main btn-display-none verification__btn verification__btn__accept w-full md:w-3/7 mr-0 md:mr-4"
                        name="accept" id="accept" type="submit">
                    ${doSubmit}
                </button>
            </div>

            <div class="w-full mt-4 test">
                <#list 1..lengthCode as x>
                    <#if x = 1>
                        <input type="text" inputmode="numeric" pattern="[0-9]*" placeholder="-" maxlength="1" id="smscode-${x}" style="font-size: 22px;"
                               name="smscode-${x}"
                                <#if isMoreThanFiveAttempts?? && isMoreThanFiveAttempts>
                                    disabled
                                <#elseif codeLimited?? && codeLimited>
                                    disabled
                                </#if>
                               class="text-center align-middle w-14 h-14 border rounded-lg focus:border-extra outline-none"
                               autocomplete="off" autofocus/>
                    <#else>
                        <input type="text" inputmode="numeric" pattern="[0-9]*" placeholder="-" maxlength="1" id="smscode-${x}" style="font-size: 22px;"
                               name="smscode-${x}"
                                <#if isMoreThanFiveAttempts?? && isMoreThanFiveAttempts>
                                    disabled
                                <#elseif codeLimited?? && codeLimited>
                                    disabled
                                </#if>
                               class="ml-4 text-center align-middle w-14 h-14 border rounded-lg focus:border-extra outline-none"
                               autocomplete="one-time-code"/>
                    </#if>

                </#list>
            </div>

            <input id="codeNumbers" name="codeNumbers" class="hidden" value="${lengthCode!}"/>
            <input id="smscode" name="smscode" class="hidden"/>

            <#if secondsUserIsBlocked gt 0>
                <input id="expirationSeconds" name="expirationSeconds" class="hidden"
                       value="${secondsUserIsBlocked?c}"/>
            <#else>
                <input id="expirationSeconds" name="expirationSeconds" class="hidden" value="${secondsCodeIsValid?c}"/>
            </#if>

            <#if isMoreThanFiveAttempts?? && isMoreThanFiveAttempts>
                <span>
                    <button class="verification__text verification__resend mt-8" name="resend"
                            type="submit">${sendAgain}</button>
                </span>
            <#else>

                <div class="flex flex-col sm:flex-row md:items-end items-center justify-between">

                <div id="timer2" class="text-black text-center md:text-right flex items-center justify-center md:justify-start
                     verification__timer__text">
                        <span style="color: #899DA8"> Код можно запросить через: </span>
                    <br>
                    <span id="timer-time" class="textTimer2"></span>
                </div>

                    <div>
                        <#if activationCodeType == "CODE_TO_SMS">
                            <button class="hidden verification__text verification__resend mt-8"
                                    id="resend" name="resend" type="submit">
                                Отправить ещё раз
                            </button>
                        <#else>
                            <button class="hidden verification__text verification__resend mt-8"
                                    id="resend" name="resend" type="submit">
                                Повторный звонок
                            </button>
                        </#if>
                    </div>

                    <div>
                        <#if activationCodeType == "CODE_BY_PHONE_NUMBER">
                            <button class="verification__text verification__resend mt-8" form="totpe" id="sentCode"
                                    name="sendPhoneCode" type="submit">
                                Отправить СМС
                            </button>
                        </#if>
                    </div>
                </div>
            </#if>

        </form>
    </#if>

    <script>

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

        var actionIsEmpty = ${actionIsEmpty?c};
        var clientIsB2B = ${clientIsB2B?c};

        if (clientIsB2B === true && actionIsEmpty === true) {
            window.onunload = function () {
                window.parent.postMessage('post-selected', '*');
                console.log("Отправлено тк B2B и Action пуст");
            };
        }
    </script>
</@layout.registrationLayout>
