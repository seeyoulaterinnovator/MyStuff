<#import "template.ftl" as layout>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayMessage=true displayCity=false; section>
<#if section = "header">
<#--lengthCode=6 - отправка смс, lengthCode=4 - звонок на телефон (depricated)-->
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
    </#if>
    <form id="totpe" action="${url.loginAction}" method="POST">
    </form>
    <form id="totpForm" action="${url.loginAction}" method="POST">
        <#--lengthCode=6 - отправка смс, lengthCode=4 - звонок на телефон (depricated)-->

        <div class="w-full xl:pb-37px md:pb-10 sm:pb-8 pb-4 center-items">
            <#list 1..lengthCode as x>
                <input placeholder="-" maxlength="1" id="smscode-${x}" style="font-size: 22px;"
                       name="smscode-${x}"
                        <#if isMoreThanFiveAttempts?? && isMoreThanFiveAttempts>
                            disabled
                        <#elseif codeLimited?? && codeLimited>
                            disabled
                        </#if>
                       class="text-center align-middle w-14 h-14 border rounded-lg focus:border-extra outline-none squares
                            sms-input"
                       autocomplete="off"/>
            </#list>
        </div>
        <input id="codeNumbers" name="codeNumbers" class="hidden" value="${lengthCode!}"/>
        <#if error?has_content>
            <input id="expirationSeconds" name="expirationSeconds" class="hidden" value="${expirationSeconds!}"/>
        <#else>
            <input id="expirationSeconds" name="expirationSeconds" class="hidden" value="${expirationSeconds!}"/>
        </#if>
        <input id="smscode" name="smscode" class="hidden"/>
        <div class="flex md:justify-start justify-start w-full items-center text-left md:text-left xl:pb-55px md:pb-10 sm:pb-8 pb-4">
            <#if isMoreThanFiveAttempts?? && isMoreThanFiveAttempts>
                <span class="w-full" style="text-align: right">
                    <button class="font-light verification__resend" name="resend"
                                    type="submit">${sendAgain}</button>
                </span>
            <#else>

                <div id="timer" style="margin-left: auto;">
                            <span style="color: #899DA8">
                                <#if codeLimited?? && codeLimited> Код можно запросить через: <#else> Код действует </#if>
                            </span>
                    <span id="timer-time" class="px-1 textTimer">
                            </span>
                </div>
                <#if enableRepeatCall?? && enableRepeatCall!>
                    <p class="hidden font-light text-black verification__text" id="resend">
                        <span class="w-full">
                            <button class="font-light verification__resend" name="resend"
                                            type="submit">${sendAgain}</button>
                        </span>
                    </p>
                </#if>
            </#if>
        </div>


        <button class="hidden"
                style="display: none"
                name="accept" id="accept" type="submit">${doSubmit}</button>

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
