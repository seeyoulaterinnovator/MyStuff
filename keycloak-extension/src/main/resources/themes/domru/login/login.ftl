<#import "template.ftl" as layout>
<#import "templates/components.ftl" as components>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayInfo=social.displayInfo displayWide=(realm.password && social.providers??); section>
<#if section = "header">
<style>
            .login-password-forgot {
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            padding: 0px;
            gap: 24px;
            width: 568px;
            height: 96px;
            flex: none;
            order: 2;
            align-self: stretch;
            flex-grow: 0;
        }


    @media only screen and (min-width: 768px) and (max-width: 1279px) {
        .login-password-forgot {
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            padding: 0px;
            gap: 18px;
            width: 480px;
            height: 85px;
            flex: none;
            order: 2;
            align-self: stretch;
            flex-grow: 0;
        }
    }

    @media only screen and (max-width: 767px) {
        .login-password-forgot {
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            padding: 0px;
            gap: 18px;
            width: 288px;
            height: 137px;
            flex: none;
            order: 2;
            align-self: stretch;
            flex-grow: 0;
        }
    }


        .login-pass {
            display: flex;
            flex-direction: row;
            align-items: center;
            padding: 0px;
            gap: 24px;
            width: 568px;
            height: 48px;
            flex: none;
            order: 0;
            align-self: stretch;
            flex-grow: 0;
        }


    @media only screen and (min-width: 768px) and (max-width: 1279px) {
        .login-pass {
            color: #16629A;
            display: flex;
            flex-direction: row;
            align-items: center;
            padding: 0px;
            gap: 18px;
            width: 480px;
            height: 44px;
            flex: none;
            order: 0;
            align-self: stretch;
            flex-grow: 0;
        }
    }

    @media only screen and (max-width: 767px) {
        .login-pass {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: flex-start;
            padding: 0px;
            gap: 18px;
            width: 288px;
            height: 98px;
            flex: none;
            order: 0;
            align-self: stretch;
            flex-grow: 0;
        }
    }

        .forgot-pass {
            display: flex;
            flex-direction: row;
            align-items: center;
            padding: 0px;
            gap: 24px;
            width: 568px;
            height: 24px;

            /* Inside auto layout */
            flex: none;
            order: 1;
            align-self: stretch;
            flex-grow: 0;
        }


    @media only screen and (min-width: 768px) and (max-width: 1279px) {
        .forgot-pass {
            gap: 18px;
            width: 480px;
            height: 23px;
        }
    }

    @media only screen and (max-width: 767px) {
        .forgot-pass {
            justify-content: center; /* center text horizontally */
            text-align: center;
        / gap: 18 px;
            width: 288px;
            height: 21px;
        }
    }

        .my-span {
            font-family: 'CoFo Sans';
            font-style: normal;
            font-weight: 400;
            display: flex;
            align-items: center;
            color: #16629A;

            /* Inside auto layout */
            flex: none;
            order: 0;
            flex-grow: 1;
        }

    @media only screen and (min-width: 768px) and (max-width: 1279px) {
        .my-span {
            width: 480px;
            height: 23px;
            font-size: 17px;
            line-height: 23px;
        }
    }

    @media only screen and (max-width: 767px) {
        .my-span {
            width: 288px;
            height: 21px;
            font-size: 16px;
            line-height: 21px;
            text-align: center;
            justify-content: center; /* center text horizontally */
        }
    }

            .numbers {
                display: flex;
                flex-direction: row;
                align-items: flex-start;
                padding: 0px;
                gap: 14px;
                width: 568px;
                height: 60px;
                flex: none;
                order: 1;
                align-self: stretch;
                flex-grow: 0;
            }
            @media only screen and (min-width: 768px) and (max-width: 1279px) {
                .numbers{
                    width: 480px;
                    height: 58px;
                }
            }
            @media only screen and (max-width: 767px) {
                .numbers{

                    height: 56px;
                    width: 100%;
                }
            }
</style>

<#if !hideRegistration!false>
    <@blocks.contentHeader mainTitle="${doLogIn}" secondaryTitle="${registerTitle}" secondaryHref="${url.registrationUrl}" withBorder=true />
<#else>
    <@blocks.contentHeader mainTitle="${doLogIn}" secondaryTitle=" " secondaryHref=" " withBorder=true />
</#if>
<#elseif section = "form">
<#if loginViaEmailOrUsernameAndPassword!true>
    <#if !isSwitcherOn!true>
    <#--        <p class="mb-7">${loginTitleText}</p>-->
        <#if realm.password>
            <form id="loginForm" class="md:flex md:flex-wrap md:justify-between"
            onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">


            <div class="field field__container field--required mb-3 sm:mb-4 md:w-full">
                <input class="hidden w-0 h-0" id="domain-login" name="city">
                <#if withCity?has_content && withCity == "TRUE">
                    <input id="withCity" class="hidden w-0 h-0" name="withCity" value="TRUE">
                </#if>
                <#if showModal?has_content>
                    <input id="showModalIframe" class="hidden w-0 h-0" name="showModal" value="${showModal}">
                </#if>
                <#if usernameEditDisabled??>
                    <input name="username" id="username" class="field__input"
                           label="${usernameOrEmailPlaceholder}"
                           placeholder="Телефон или E-mail" value="${(login.username!)}"
                           type="text" disabled/>
                <#else>
                        <input name="username" id="username" class="field__input"
                               label="${usernameOrEmailPlaceholder}"
                               placeholder="Телефон или E-mail" value="${(login.username!)}"
                               type="text" autofocus autocomplete="off"/>
                        <span> ${error!}</span>

                </#if>
                <label class="field__label" for="username">Телефон или E-mail</label>
            </div>


            <@components.field class="mb-7 sm:mb-8 md:w-full" fieldName="password" label="Пароль" placeholder="Пароль" type="password" required=true />

            <div class="login-password-forgot">
            <div class="login-pass">
                <button id="submit" name="loginPasswordButton" class="btn btn-main btn-enter"
                        type="submit">${enter}</button>
                <button id="topSecretButton" type="button" class="btn btn-switcher text-accentBlue-900"
                        onclick="document.getElementById('smsLoginButton').click();">Получить временный код
                </button>
            </div>
            <div class="forgot-pass">
                <#if realm.resetPasswordAllowed>
                    <span class="my-span">
                            <a href="${url.loginResetCredentialsUrl}">${doForgotPassword}</a>
            </span>
                </#if>
            </div>
        </#if>
        </div>
        </form>

        <form id="on" method="POST" name="on" action="${url.loginUrl}">
            <button id="smsLoginButton" name="on" type="submit" class="hidden">
                login via sms
            </button>
        </form>


    <#elseif isSwitcherOn!false>
        <p class="mb-7">Мы отправим код в СМС</p>
        <#if realm.password>
            <form id="loginForm" class="md:flex md:flex-wrap md:justify-between"
            onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
            <p class="pb-2 sm:pb-3 md:pb-4 text-accentRed-1100"> ${error!}<p>
            <div class="field field__container field--required mb-3 sm:mb-4 md:w-full">
                <input class="hidden w-0 h-0" id="domain-login" name="city">
                <#if withCity?has_content && withCity == "TRUE">
                    <input id="withCity" class="hidden w-0 h-0" name="withCity" value="TRUE">
                </#if>
                <#if showModal?has_content>
                    <input id="showModalIframe" class="hidden w-0 h-0" name="showModal" value="${showModal}">
                </#if>
                <#if usernameEditDisabled??>
                    <input name="username" id="username-second" class="field__input"
                           label="${usernameOrEmailPlaceholder}"
                           placeholder="Номер телефона" value="${(login.username!)}"
                           type="text" disabled/>
                <#else>
                    <input name="username" id="username-second" class="field__input"
                           label="${usernameOrEmailPlaceholder}"
                           placeholder="Номер телефона" value="${(login.username!)}"
                           type="text" autofocus autocomplete="off"/>
                </#if>
                <label class="field__label" for="username-second">Номер телефона</label>
            </div>
        </#if>



        <div class="login-password-forgot">
        <div class="login-pass">
            <button id="submit-phone" name="smsButton" class="btn btn-main btn-enter btn-new-enter" type="submit">${enter}</button>
            <button id="topSecretButton" type="button" class="btn btn-switcher text-accentBlue-900"
                    onclick="document.getElementById('loginPasswordButton').click();">Войти с помощью логина
            </button>
        </div>
        </div>



        </form>

        <form id="off" method="POST" name="off" action="${url.loginUrl}">
            <button id="loginPasswordButton" name="off" type="submit" class="hidden">
                яяяяяяяя
            </button>
        </form>

    </#if>



<#elseif loginViaSms!true>
    <#if !isSwitcherOn!true>
        <p class="mb-7">Мы отправим код в СМС</p>
        <#if realm.password>
            <form id="loginForm" class="md:flex md:flex-wrap md:justify-between"
            onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
            <p class="pb-2 sm:pb-3 md:pb-4 text-accentRed-1100"> ${error!}<p>
            <div class="field field__container field--required mb-3 sm:mb-4 md:w-full">
                <input class="hidden w-0 h-0" id="domain-login" name="city">
                <#if withCity?has_content && withCity == "TRUE">
                    <input id="withCity" class="hidden w-0 h-0" name="withCity" value="TRUE">
                </#if>
                <#if showModal?has_content>
                    <input id="showModalIframe" class="hidden w-0 h-0" name="showModal" value="${showModal}">
                </#if>
                <#if usernameEditDisabled??>
                    <input name="username" id="username-second" class="field__input"
                           label="${usernameOrEmailPlaceholder}"
                           placeholder="Номер телефона" value="${(login.username!)}"
                           type="text" disabled/>
                <#else>
                    <input name="username" id="username-second" class="field__input"
                           label="${usernameOrEmailPlaceholder}"
                           placeholder="Номер телефона" value="${(login.username!)}"
                           type="text" autofocus autocomplete="off"/>
                </#if>
                <label class="field__label" for="username-second">Номер телефона</label>
            </div>
        </#if>
        <div class="flex justify-between w-full items-center">
            <button id="submit-phone" name="smsButton" class="btn btn-main btn-enter" type="submit">${enter}</button>
            <button id="topSecretButton" type="button" class="btn"
                    onclick="document.getElementById('loginPasswordButton').click();">телефон
            </button>
        </div>
        </form>
        <form method="POST" action="${url.loginUrl}">
            <button id="loginPasswordButton" name="on" type="submit" class="hidden">
                Войти с помощью логина
            </button>
        </form>
    <#elseif isSwitcherOn!false>
        <p class="mb-7">${loginTitleText}</p>
        <#if realm.password>
            <form id="loginForm" class="md:flex md:flex-wrap md:justify-between"
            onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
            <p class="pb-2 sm:pb-3 md:pb-4 text-accentRed-1100"> ${error!}<p>
            <div class="field field__container field--required mb-3 sm:mb-4 md:w-full">
                <input class="hidden w-0 h-0" id="domain-login" name="city">
                <#if withCity?has_content && withCity == "TRUE">
                    <input id="withCity" class="hidden w-0 h-0" name="withCity" value="TRUE">
                </#if>
                <#if showModal?has_content>
                    <input id="showModalIframe" class="hidden w-0 h-0" name="showModal" value="${showModal}">
                </#if>
                <#if usernameEditDisabled??>
                    <input name="username" id="username" class="field__input"
                           label="${usernameOrEmailPlaceholder}"
                           placeholder="Телефон или E-mail" value="${(login.username!)}"
                           type="text" disabled/>
                <#else>
                    <input name="username" id="username" class="field__input"
                           label="${usernameOrEmailPlaceholder}"
                           placeholder="Телефон или E-mail" value="${(login.username!)}"
                           type="text" autofocus autocomplete="off"/>
                </#if>
                <label class="field__label" for="username">${usernameOrEmailPlaceholder}</label>
            </div>

            <@components.field class="mb-7 sm:mb-8 md:w-full" fieldName="password" label="${passwordPlaceholder}" placeholder="Пароль" type="password" required=true />

            <div class="flex justify-between w-full items-center">
            <button id="submit" name="loginPasswordButton" class="btn btn-main btn-enter"
                    type="submit">${enter}</button>
            <button id="topSecretButton" type="button" class="btn"
                    onclick="document.getElementById('smsLoginButton').click();">телефон
            </button>
            <#if realm.resetPasswordAllowed>
                <span class="reset-password">
                            <a href="${url.loginResetCredentialsUrl}">${doForgotPassword}</a>
            </span>
            </#if>
        </#if>
        </div>
        </form>
        <form method="POST" name="smsLoginButton" action="${url.loginUrl}">
            <button id="smsLoginButton" name="off" type="submit" class="hidden">
                login via sms
            </button>
        </form>
        <#if realm.password && social.providers??>
            <div class="flex items-center mt-12">
                <div class="text-no-wrap text-with-login mr-6">${loginWith}</div>
                <ul class="logo-social-providers">
                    <#list social.providers as p>
                        <li class="mr-4">
                            <a href="${p.loginUrl}">
                                <div class="logo logo--${p.providerId}"></div>
                            </a>
                        </li>
                    </#list>
                </ul>
            </div>
        </#if>
    </#if>


<#elseif loginViaPhoneCall>
    <#if !isSwitcherOn!true>
        <p class="mb-7">Мы вам перезвоним. Отвечать на звонок не нужно. Запомните последние 4 цифры номера входящего
            звона</p>
        <#if realm.password>
            <form id="loginForm" class="md:flex md:flex-wrap md:justify-between"
            onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
            <p class="pb-2 sm:pb-3 md:pb-4 text-accentRed-1100"> ${error!}<p>
            <div class="field field__container field--required mb-3 sm:mb-4 md:w-full">
                <input class="hidden w-0 h-0" id="domain-login" name="city">
                <#if withCity?has_content && withCity == "TRUE">
                    <input id="withCity" class="hidden w-0 h-0" name="withCity" value="TRUE">
                </#if>
                <#if showModal?has_content>
                    <input id="showModalIframe" class="hidden w-0 h-0" name="showModal" value="${showModal}">
                </#if>
                <#if usernameEditDisabled??>
                    <input name="username" id="username-second" class="field__input" label="Enter phone number"
                           placeholder="Номер телефона" value="${(login.username!)}"
                           type="text" disabled/>
                <#else>
                    <input name="username" id="username-second" class="field__input" label="Enter phone number"
                           placeholder="Номер телефона" value="${(login.username!)}"
                           type="text" autofocus autocomplete="off"/>
                </#if>
                <label class="field__label" for="username-second">Номер телефона</label>
            </div>
        </#if>

        <div class="flex justify-between w-full items-center">
            <button id="submit-phone" name="phoneCallButton" class="btn btn-main btn-enter"
                    type="submit">${enter}</button>
            <button id="topSecretButton" type="button" class="btn"
                    onclick="document.getElementById('loginPasswordButton').click();">телефон
            </button>
        </div>
        </form>
        <form method="POST" action="${url.loginUrl}">
            <button id="loginPasswordButton" name="on" type="submit" class="hidden">
                Войти с помощью логина
            </button>
        </form>


    <#elseif isSwitcherOn!false>
        <p class="mb-7">${loginTitleText}</p>
        <#if realm.password>
            <form id="loginForm" class="md:flex md:flex-wrap md:justify-between"
            onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
            <p class="pb-2 sm:pb-3 md:pb-4 text-accentRed-1100"> ${error!}<p>
            <div class="field field__container field--required mb-3 sm:mb-4 md:w-full">
                <input class="hidden w-0 h-0" id="domain-login" name="city">
                <#if withCity?has_content && withCity == "TRUE">
                    <input id="withCity" class="hidden w-0 h-0" name="withCity" value="TRUE">
                </#if>
                <#if showModal?has_content>
                    <input id="showModalIframe" class="hidden w-0 h-0" name="showModal" value="${showModal}">
                </#if>
                <#if usernameEditDisabled??>
                    <input name="username" id="username" class="field__input"
                           label="${usernameOrEmailPlaceholder}"
                           placeholder="Телефон или E-mail" value="${(login.username!)}"
                           type="text" disabled/>
                <#else>
                    <input name="username" id="username" class="field__input"
                           label="${usernameOrEmailPlaceholder}"
                           placeholder="Телефон или E-mail" value="${(login.username!)}"
                           type="text" autofocus autocomplete="off"/>
                </#if>
                <label class="field__label" for="username">Телефон или E-mail</label>
            </div>

            <@components.field class="mb-7 sm:mb-8 md:w-full" fieldName="password" label="Пароль" placeholder="Пароль" type="password" required=true />

            <div class="flex justify-between w-full items-center">
            <button id="submit" name="loginPasswordButton" class="btn btn-main btn-enter"
                    type="submit">${enter}</button>
            <button id="topSecretButton" type="button" class="btn"
                    onclick="document.getElementById('smsLoginButton').click();">телефон
            </button>
            <#if realm.resetPasswordAllowed>
                <span class="reset-password">
                            <a href="${url.loginResetCredentialsUrl}">${doForgotPassword}</a>
            </span>
            </#if>
        </#if>
        </div>
        </form>
        <form method="POST" action="${url.loginUrl}">
            <button id="smsLoginButton" name="off" type="submit" class="hidden">
                login via phone call
            </button>
        </form>
        <#if realm.password && social.providers??>
            <div class="flex items-center mt-12">
                <div class="text-no-wrap text-with-login mr-6">${loginWith}</div>
                <ul class="logo-social-providers">
                    <#list social.providers as p>
                        <li class="mr-4">
                            <a href="${p.loginUrl}">
                                <div class="logo logo--${p.providerId}"></div>
                            </a>
                        </li>
                    </#list>
                </ul>
            </div>
        </#if>
    </#if>
</#if>

<#if secondPhaseLogin!false>
<h3 class="verification__sub  pb-2 sm:pb-3 md:pb-4" x-ms-format-detection="none">

    <#if smsMessage!true>
        Введите код из СМС
    <#else>
        Последние 4 цифры звонка
    </#if>
    <form id="totpe" action="${url.loginAction}" method="POST">
    </form>
    <form id="totpForm" action="${url.loginAction}" method="POST">
        <#--        <p class="pb-2 sm:pb-3 md:pb-4 text-accentRed-1100"> ${error!}<p>-->
        <div class="numbers">
            <#list 1..lengthCode as x>
                <input placeholder="-" maxlength="1" id="smscode-${x}" style="font-size: 22px;" name="smscode-${x}"
                       class="text-center align-middle w-10 h-10 sm:w-14 sm:h-14 border rounded-lg focus:border-extra outline-none"
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
            <div id="timer"
                 class="text-black text-center md:text-right flex items-center my-6 md:my-0 justify-center md:justify-start">
                Код действует <span id="timer-time" class="px-1 textTimer"></span><span
                        style="font-weight: 350;font-size: 13px;line-height: 16px;color: #7585A1;opacity: 0.8;">м:c</span>
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
            <button id="topSecretButton" type="button" class="btn"
                    onclick="document.getElementById('loginPasswordButton').click();">телефон
            </button>
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


    <#--    <#if realm.password && social.providers??>-->
    <#--        <div class="flex items-center mt-12">-->
    <#--            <div class="text-no-wrap text-with-login mr-6">${loginWith}</div>-->
    <#--            <ul class="logo-social-providers">-->
    <#--                <#list social.providers as p>-->
    <#--                    <li class="mr-4">-->
    <#--                        <a href="${p.loginUrl}">-->
    <#--                            <div class="logo logo--${p.providerId}"></div>-->
    <#--                        </a>-->
    <#--                    </li>-->
    <#--                </#list>-->
    <#--            </ul>-->
    <#--        </div>-->

    <#--    </#if>-->
    </#if>

    <script>
        window.addEventListener('message', function (event) {
            if (event.origin !== document.location.origin) {
                var loginForm = document.getElementById('loginForm');
                var actionAttribute = loginForm.getAttribute("action");
                if (actionAttribute.indexOf("iframe") === -1) {
                    actionAttribute = actionAttribute + "&iframe=1";
                }
                loginForm.setAttribute("action", actionAttribute);
            }
        });

        function submitForm1() {
            document.getElementById("on").submit();
        }

    </script>
    </@layout.registrationLayout>
