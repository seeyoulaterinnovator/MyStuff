<#import "template.ftl" as layout>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayMessage=false displayCity=false; section>
    <#if section = "header">
        <#--lengthCode=6 - отправка смс, lengthCode=4 - звонок на телефон -->
        <#if lengthCode==6>
            <@blocks.verificationHeader mainTitle="Вам выслан одноразовый пароль на номер:" />
        <#else>
            <#if enableRepeatCall?? && enableRepeatCall!>
                <@blocks.verificationHeader mainTitle="Введите последние 4 цифры номера, входящего звонка на номер:" />
            <#else>
                <@blocks.verificationHeader mainTitle="Введите код, отправленый Вам на электронную почту:" />
            </#if>
        </#if>
    <#elseif section = "form">
        <#if lengthCode==4 && !enableRepeatCall && userEmail??>
            <h3 class="verification__sub pb-2 sm:pb-3 md:pb-4">${userEmail}</h3>
        <#elseif userPhone??>
            <h3 class="verification__sub pb-2 sm:pb-3 md:pb-4" x-ms-format-detection="none">${userPhone?replace('([0-9]{1})([0-9]{3})([0-9]{3})([0-9]{2})([0-9]{2})',
                '+$1 ($2) $3-$4-$5', 'ri')}</h3>
        </#if>
        <form id="totpe" action="${url.loginAction}" method="POST">
         </form>
        <form id="totpForm" action="${url.loginAction}" method="POST">
        <#--lengthCode=6 - отправка смс, lengthCode=4 - звонок на телефон -->
        <p class="pb-2 sm:pb-3 md:pb-4 text-accentRed"> ${error!}<p>
            <div class="flex justify-between w-full xl:pb-37px md:pb-10 sm:pb-8 pb-4">
                <#list 1..lengthCode as x>
                    <input placeholder="-" maxlength="1" id="smscode-${x}" name="smscode-${x}" class="text-center align-middle text-3xl w-10 h-10 sm:w-14 sm:h-14 border rounded-lg focus:border-extra outline-none" autocomplete="off" />
                </#list>
            </div>
            <input id="codeNumbers" name="codeNumbers" class="hidden" value="${lengthCode!}" />
            <#if error?has_content>
                <input id="expirationSeconds" name="expirationSeconds" class="hidden" value="0" />
            <#else>
                <input id="expirationSeconds" name="expirationSeconds" class="hidden" value="${expirationSeconds!}" />
            </#if>

            <input id="smscode" name="smscode" class="hidden" />

            <div class="flex md:justify-start justify-center w-full items-center text-center md:text-right xl:pb-55px md:pb-10 sm:pb-8 pb-4">
                <#if lengthCode==4>
                    <button class="border-b hoverable border-dashed text-black-50 text-right hidden" form="totpe" id="sentCode" name="sendEmailCode" type="submit">${sendByEmail}</button>
                </#if>
            </div>
            <div class="sm:block md:flex justify-between w-full items-center text-center md:text-left">
                <button class="btn btn-main w-full md:w-3/7 mr-0 md:mr-4" name="accept" id="accept" type="submit">${doSubmit}</button>

                <div id="timer" class="text-main-600 text-center md:text-right text-sm flex items-center my-6 md:my-0 justify-center md:justify-start">
                    Пароль действует <span id="timer-time" class="px-1 text-black text-5/3em"></span> мин
                </div>
                <#if lengthCode==6>
                    <button class="hidden border-b hoverable border-dashed text-black-50 text-center md:text-right my-6 md:my-0" name="resend" id="resend" type="submit" >${sendAgain}</button>
                <#elseif enableRepeatCall?? && enableRepeatCall!>
                    <button class="hidden border-b hoverable border-dashed text-black-50 text-center md:text-right my-6 md:my-0" name="resend" id="resend" type="submit" >${sendAgain}</button>
                </#if>

            </div>
        </form>
    </#if>
</@layout.registrationLayout>
