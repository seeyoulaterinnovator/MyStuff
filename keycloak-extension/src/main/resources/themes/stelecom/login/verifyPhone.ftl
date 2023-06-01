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
                <#if activationCodeType == "CODE_TO_SMS">
                    Вам выслан одноразовый пароль на номер:
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
        <p class="pb-2 sm:pb-3 md:pb-4 text-accentRed"> ${error!}<p>
            <div class="flex justify-between w-full xl:pb-37px md:pb-10 sm:pb-8 pb-4">
                <#list 1..lengthCode as x>
                    <input placeholder="-" maxlength="1" id="smscode-${x}" name="smscode-${x}"
                            <#if isMoreThanFiveAttempts?? && isMoreThanFiveAttempts>
                                disabled
                            <#elseif codeLimited?? && codeLimited>
                                disabled
                            </#if>
                           class="text-center align-middle text-3xl w-10 h-10 sm:w-14 sm:h-14 border rounded-lg focus:border-extra outline-none" autocomplete="off" autofocus/>
                </#list>
            </div>
            <input id="codeNumbers" name="codeNumbers" class="hidden" value="${lengthCode!}" />
            <input id="smscode" name="smscode" class="hidden" />

            <#if secondsUserIsBlocked gt 0>
                <input id="expirationSeconds" name="expirationSeconds" class="hidden" value="${secondsUserIsBlocked?c}"/>
            <#else>
                <input id="expirationSeconds" name="expirationSeconds" class="hidden" value="${secondsCodeIsValid?c}"/>
            </#if>

            <div class=" flex flex-col md:items-start items-center justify-between">

                <div id="timer" class="text-black text-center md:text-right flex items-center md:my-0 justify-center md:justify-start
                     verification__timer__text">
                        <span style="color: #899DA8"> Код можно запросить через: </span>
                    <span id="timer-time" class="px-2 textTimer"></span>
                </div>

                <div>
                    <#if activationCodeType == "CODE_TO_SMS">
                        <button class="hidden verification__text verification__resend"
                                id="resend" name="resend" type="submit">
                            Отправить ещё раз
                        </button>
                    <#else>
                        <button class="hidden verification__text verification__resend"
                                id="resend" name="resend" type="submit">
                            Повторный звонок
                        </button>
                    </#if>
                </div>

                <div>
                    <#if activationCodeType == "CODE_BY_PHONE_NUMBER">
                        <button class="verification__text verification__resend mt-4" form="totpe" id="sentCode"
                                name="sendPhoneCode" type="submit">
                            Отправить СМС
                        </button>
                    </#if>
                </div>
            </div>

            <div class="sm:block md:flex mt-8 w-full items-center text-center md:text-left">
                <button class="btn btn-main verification__btn verification__btn__accept w-full md:w-3/7 mr-0 md:mr-4" name="accept" id="accept" type="submit">Подтвердить</button>
                <#if lengthCode==4>
                    <button class="btn verification__btn verification__btn__send" form="totpe" id="sentCode" name="sendEmailCode" type="submit">Отправить на эл. почту</button>
                </#if>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>
