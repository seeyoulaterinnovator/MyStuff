<#import "template.ftl" as layout>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayMessage=false displayCity=false; section>
    <#if section = "header">
        <style>
            .back-timer {
                display: flex;
                flex-direction: row;
                align-items: center;
                padding: 0px;
                gap: 24px;

                width: 568px;
                height: 48px;


                /* Inside auto layout */

                flex: none;
                order: 2;
                align-self: stretch;
                flex-grow: 0;
            }

            @media only screen and (min-width: 768px) and (max-width: 1279px) {
                .back-timer {

                    gap: 18px;

                    width: 480px;
                    height: 44px;

                }
            }

            @media only screen and (max-width: 767px) {
                .back-timer {
                    flex-direction: column;
                    justify-content: center;
                    gap: 10px;
                    width: 100%;
                    height: auto;
                }

                #timer {
                    margin: 0 auto;
                    text-align: center;
                }
            }

            .center-items {

            }

            @media (max-width: 767px) {
                .center-items {
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }
            }

        </style>
        <@blocks.contentHeader mainTitle="Вход" secondaryTitle="Регистрация" secondaryHref="${url.registrationUrl}" withBorder=true />

    <#elseif section = "form">
        <#if phoneCallButton!false>
            <h3 class="verification__sub pb-2 sm:pb-3 md:pb-4">
                На указанный номер поступит звонок. Для подтверждения нужно ввести последние 4 цифры входящего номера
            </h3>
        <#else>
            <h3 class="verification__sub pb-2 sm:pb-3 md:pb-4">
                Введите код из СМС отправленный на указанный номер телефона
            </h3>
        </#if>
        <form id="totpe" action="${url.loginAction}" method="POST"></form>

        <form id="totpForm" action="${url.loginAction}" method="POST">

            <div class="w-full xl:pb-37px md:pb-10 sm:pb-8 pb-4 center-items">
                <#list 1..lengthCode as x>
                    <#if x = 1>
                        <input placeholder="-" maxlength="1" id="smscode-${x}" style="font-size: 22px;"
                               name="smscode-${x}"
                               class="text-center align-middle w-14 h-14 border rounded-lg focus:border-extra outline-none"
                               autocomplete="off"/>
                    <#else>
                        <input placeholder="-" maxlength="1" id="smscode-${x}" style="font-size: 22px;"
                               name="smscode-${x}"
                               class="ml-4 text-center align-middle w-14 h-14 border rounded-lg focus:border-extra outline-none"
                               autocomplete="off"/>
                    </#if>
                </#list>
            </div>

            <input id="codeNumbers" name="codeNumbers" class="hidden" value="${lengthCode!}"/>
            <input id="expirationSeconds" name="expirationSeconds" class="hidden" value="${expirationSeconds!}"/>
            <input id="smscode" name="smscode" class="hidden"/>

            <div class="back-timer">
                <div>
                    <button id="topSecretButton" type="button" class="btn btn-back text-accentBlue-900"
                            onclick="document.getElementById('loginPasswordButton').click();">телефон
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
                                    type="submit">${sendAgain}</button>
                        </span>
                    </p>
                </#if>
            </div>
            <div class="sm:block md:flex w-full items-center text-center md:text-left">
                <button class="hidden"
                        name="accept" id="accept" type="submit">${doSubmit}</button>

            </div>
        </form>
        <form method="POST" action="${url.loginUrl}">
            <button id="loginPasswordButton" name="on" type="submit" class="hidden">
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
