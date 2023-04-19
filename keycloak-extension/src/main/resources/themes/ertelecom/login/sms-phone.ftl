<#import "template.ftl" as layout>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayMessage=false displayCity=false; section>
    <#if section = "header">
        <@blocks.contentHeader mainTitle="Вход" secondaryTitle="" secondaryHref="" withBorder=true />

    <#elseif section = "form">
        <#if phoneCallButton!false>
            <h3 class="verification__sub pb-2 sm:pb-3 md:pb-4 info-text">
                На указанный номер поступит звонок. Для подтверждения нужно ввести последние 4 цифры входящего номера
            </h3>
        <#else>
            <h3 class="verification__sub pb-2 sm:pb-3 md:pb-4 info-text">
                Введите код из СМС отправленный на указанный номер телефона
            </h3>
        </#if>
        <form id="totpe" action="${url.loginAction}" method="POST"></form>

        <form id="totpForm" action="${url.loginAction}" method="POST">

            <div class="flex justify-between w-full xl:pb-37px md:pb-10 sm:pb-8 pb-4">
                <#list 1..lengthCode as x>
                    <input placeholder="*" maxlength="1" id="smscode-${x}" style="font-size: 22px; border-bottom: 2px solid #000000" name="smscode-${x}" class="text-center align-middle w-10 h-10 sm:w-14 sm:h-14 outline-none" autocomplete="off" />
                </#list>
            </div>

            <input id="codeNumbers" name="codeNumbers" class="hidden" value="${lengthCode!}"/>
            <input id="expirationSeconds" name="expirationSeconds" class="hidden" value="${expirationSeconds!}"/>
            <input id="smscode" name="smscode" class="hidden"/>

            <div class="back-timer">
                <div>
                    <button id="topSecretButton" type="button" class="btn btn-back text-accentBlue-900"
                            onclick="document.getElementById('loginPasswordButton').click();">Войти с помощью логина
                    </button>
                </div>
                <div id="timer" style="margin-left: auto;">
                    <span style="color: #899DA8">
                        Код действует:
                    </span>
                    <span id="timer-time" class="px-2 textTimer">
                    </span>
                </div>
                <#if enableRepeatCall?? && enableRepeatCall!>
                    <p class="hidden font-light text-black verification__text" id="resend">
                        password ne prihodit?
                        <span>
                            <button class="font-light verification__resend" name="resend"
                                    type="submit">Отправить еще раз</button>
                        </span>
                    </p>
                </#if>
            </div>
            <div class="sm:block md:flex w-full items-center text-center md:text-left">
                <button class="hidden"
                        name="accept" id="accept" type="submit">Подтвердить</button>

            </div>
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
    </script>
</@layout.registrationLayout>
