<#import "template.ftl" as layout>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayMessage=true displayCity=false; section>
    <#if section = "header">
        <#--lengthCode=6 - отправка смс, lengthCode=4 - звонок на телефон -->
        <@blocks.verificationHeader mainTitle="Подтвердить" />
    <#elseif section = "form">
        <#if lengthCode==4 && !enableRepeatCall && userEmail??>
            <h3 class="verification__sub pb-2 sm:pb-3 md:pb-4">
                Введите код, отправленый Вам на электронную почту: <br/>
                ${userEmail}
            </h3>
        <#elseif userPhone??>
            <h3 class="verification__sub pb-2 sm:pb-3 md:pb-4" x-ms-format-detection="none">
                <#if isSms?? && isSms>
                    Введите код из СМС
                <#else>
                    Введите последние 4 цифры номера входящего звонка на номер:
                </#if>
                <br/>
                ${userPhone?replace('([0-9]{1})([0-9]{3})([0-9]{3})([0-9]{2})([0-9]{2})', '+$1 $2 $3 $4 $5', 'ri')}
            </h3>
        </#if>
        <form id="totpe" action="${url.loginAction}" method="POST">
         </form>
        <form id="totpForm" action="${url.loginAction}" method="POST">
        <#--lengthCode=6 - отправка смс, lengthCode=4 - звонок на телефон -->
        <p class="pb-2 sm:pb-3 md:pb-4 text-accentRed-1100"> ${error!}<p>
            <div class="flex justify-between w-full xl:pb-37px md:pb-10 sm:pb-8 pb-4">
                <#list 1..lengthCode as x>
                    <input placeholder="*" maxlength="1" id="smscode-${x}" style="font-size: 22px; border-bottom: 2px solid #000000" name="smscode-${x}"
                            <#if isMoreThanFiveAttempts?? && isMoreThanFiveAttempts>
                                disabled
                            <#elseif codeLimited?? && codeLimited>
                                disabled
                            </#if>
                           class="text-center align-middle w-10 h-10 sm:w-14 sm:h-14 outline-none" autocomplete="off" />
                </#list>
            </div>
            <input id="codeNumbers" name="codeNumbers" class="hidden" value="${lengthCode!}" />
            <#if error?has_content>
                <input id="expirationSeconds" name="expirationSeconds" class="hidden" value="${expirationSeconds!}" />
            <#else>
                <input id="expirationSeconds" name="expirationSeconds" class="hidden" value="${expirationSeconds!}" />
            </#if>

            <input id="smscode" name="smscode" class="hidden" />

            <div class="flex md:justify-start justify-start w-full items-center text-left md:text-left xl:pb-55px md:pb-10 sm:pb-8 pb-4">
            <#if isMoreThanFiveAttempts?? && isMoreThanFiveAttempts>
                <span>
                    <button class="font-light verification__resend w-full" style="text-align: right" name="resend"
                                    type="submit">Отправить еще раз</button>
                </span>
            <#else>
                <div id="timer" class="text-black text-center md:text-right flex items-center my-6 md:my-0 justify-center md:justify-start">
                    <#if codeLimited?? && codeLimited> Код можно запросить через: <#else> Код действует </#if> <span id="timer-time" class="px-1 textTimer"></span><span style="font-weight: 350;font-size: 13px;line-height: 16px;color: #7585A1;opacity: 0.8;">чч:мм:cc</span>
                </div>
                <#if enableRepeatCall?? && enableRepeatCall!>
                    <p class="hidden font-light text-black verification__text" id="resend">
                        Не приходит пароль?
                        <span>
                            <button class="font-light verification__resend" name="resend" type="submit">Отправить еще раз</button>
                        </span>
                    </p>
                </#if>
            </div>
            </#if>
            <button class="hidden"
                    style="display: none"
                    name="accept" id="accept" type="submit">${doSubmit}
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
        </script>
</@layout.registrationLayout>
