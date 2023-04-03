<#import "template.ftl" as layout>
<#import "templates/components.ftl" as components>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayInfo=social.displayInfo displayWide=(realm.password && social.providers??); section>
<#if section = "header">
<#if !hideRegistration!false>
    <@blocks.contentHeader mainTitle="${doLogIn}" secondaryTitle="${registerTitle}" secondaryHref="${url.registrationUrl}" withBorder=true />
<#else>
    <@blocks.contentHeader mainTitle="${doLogIn}" secondaryTitle=" " secondaryHref=" " withBorder=true />
</#if>
<#elseif section = "form">
<#if loginViaEmailOrUsernameAndPassword!true>
    <#if !isSwitcherOn!true>
        <p class="mb-7">${loginTitleText}</p>
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
                           placeholder="${usernameOrEmailPlaceholder}" value="${(login.username!)}"
                           type="text" disabled/>
                <#else>
                    <input name="username" id="username" class="field__input"
                           label="${usernameOrEmailPlaceholder}"
                           placeholder="${usernameOrEmailPlaceholder}" value="${(login.username!)}"
                           type="text" autofocus autocomplete="off"/>

                </#if>
                <label class="field__label" for="username">${usernameOrEmailPlaceholder}</label>
            </div>

            <@components.field class="mb-7 sm:mb-8 md:w-full" fieldName="password" label="${passwordPlaceholder}" placeholder="${passwordPlaceholder}" type="password" required=true />

            <div class="flex justify-between w-full items-center">
            <button id="submit" name="loginPasswordButton" class="btn btn-main btn-enter"
                    type="submit">${enter}</button>
            <#if realm.resetPasswordAllowed>
                <span class="reset-password">
                            <a href="${url.loginResetCredentialsUrl}">${doForgotPassword}</a>
            </span>
            </#if>
        </#if>
        </div>
        </form>
        <form method="POST" action="${url.loginUrl}">
            <button name="on" type="submit">
                login via sms
            </button>
        </form>

    <#elseif isSwitcherOn!false>
        <p class="mb-7">SMS will be sent to provided phone number</p>
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
                    <input name="username" id="username-second" class="field__input"
                           label="${usernameOrEmailPlaceholder}"
                           placeholder="${usernameOrEmailPlaceholder}" value="${(login.username!)}"
                           type="text" disabled/>
                <#else>
                    <input name="username" id="username-second" class="field__input"
                           label="${usernameOrEmailPlaceholder}"
                           placeholder="${usernameOrEmailPlaceholder}" value="${(login.username!)}"
                           type="text" autofocus autocomplete="off"/>
                </#if>
                <label class="field__label" for="username">Enter phone number</label>
            </div>
        </#if>
        <div class="flex justify-between w-full items-center">
            <button id="submit-phone" name="smsButton" class="btn btn-main btn-enter" type="submit">${enter}</button>
        </div>
        </form>
        <form method="POST" action="${url.loginUrl}">
            <button name="off" type="submit">
                Войти с помощью логина
            </button>
        </form>
    </#if>




<#elseif loginViaSms!true>
    <#if !isSwitcherOn!true>
        <p class="mb-7">Мы отправим код в СМС</p>
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
                    <input name="username" id="username" class="field__input" label="Enter phone number"
                           placeholder="Номер телефона" value="${(login.username!)}"
                           type="text" disabled/>
                <#else>
                    <input name="username" id="username" class="field__input" label="Enter phone number"
                           placeholder="Номер телефона" value="${(login.username!)}"
                           type="text" autofocus autocomplete="off"/>
                </#if>
                <label class="field__label" for="username">Enter phone number</label>
            </div>
        </#if>

        <div class="flex justify-between w-full items-center">
            <button id="submit-phone" class="btn btn-main btn-enter" name="smsButton" type="submit">${enter}</button>
        </div>
        </form>
        <form method="POST" action="${url.loginUrl}">
            <button name="on" type="submit">
                Войти с помощью логина
            </button>
        </form>
    <#elseif isSwitcherOn!false>
        <p class="mb-7">${loginTitleText}</p>
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
                           placeholder="${usernameOrEmailPlaceholder}" value="${(login.username!)}"
                           type="text" disabled/>
                <#else>
                    <input name="username" id="username" class="field__input"
                           label="${usernameOrEmailPlaceholder}"
                           placeholder="${usernameOrEmailPlaceholder}" value="${(login.username!)}"
                           type="text" autofocus autocomplete="off"/>
                </#if>
                <label class="field__label" for="username">${usernameOrEmailPlaceholder}</label>
            </div>

            <@components.field class="mb-7 sm:mb-8 md:w-full" fieldName="password" label="${passwordPlaceholder}" placeholder="${passwordPlaceholder}" type="password" required=true />

            <div class="flex justify-between w-full items-center">
            <button id="submit" name="loginPasswordButton" class="btn btn-main btn-enter"
                    type="submit">${enter}</button>
            <#if realm.resetPasswordAllowed>
                <span class="reset-password">
                            <a href="${url.loginResetCredentialsUrl}">${doForgotPassword}</a>
            </span>
            </#if>
        </#if>
        </div>
        </form>
        <form method="POST" name="loginPasswordButton" action="${url.loginUrl}">
            <button name="off" type="submit">
                login via sms
            </button>
        </form>
    </#if>


<#elseif loginViaPhoneCall>
    <#if !isSwitcherOn!true>
        <p class="mb-7">4 last numbers of phone call</p>
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
                    <input name="username" id="username" class="field__input" label="Enter phone number"
                           placeholder="${usernameOrEmailPlaceholder}" value="${(login.username!)}"
                           type="text" disabled/>
                <#else>
                    <input name="username" id="username" class="field__input" label="Enter phone number"
                           placeholder="Enter phone number" value="${(login.username!)}"
                           type="text" autofocus autocomplete="off"/>
                </#if>
                <label class="field__label" for="username">Enter phone number</label>
            </div>
        </#if>

        <div class="flex justify-between w-full items-center">
            <button id="submit-phone" name="phoneCallButton" class="btn btn-main btn-enter"
                    type="submit">${enter}</button>
        </div>
        </form>
        <form method="POST" action="${url.loginUrl}">
            <button name="on" type="submit">
                Войти с помощью логина
            </button>
        </form>
    <#elseif isSwitcherOn!false>
        <p class="mb-7">${loginTitleText}</p>
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
                           placeholder="${usernameOrEmailPlaceholder}" value="${(login.username!)}"
                           type="text" disabled/>
                <#else>
                    <input name="username" id="username" class="field__input"
                           label="${usernameOrEmailPlaceholder}"
                           placeholder="${usernameOrEmailPlaceholder}" value="${(login.username!)}"
                           type="text" autofocus autocomplete="off"/>
                </#if>
                <label class="field__label" for="username">${usernameOrEmailPlaceholder}</label>
            </div>

            <@components.field class="mb-7 sm:mb-8 md:w-full" fieldName="password" label="${passwordPlaceholder}" placeholder="${passwordPlaceholder}" type="password" required=true />

            <div class="flex justify-between w-full items-center">
            <button id="submit" name="loginPasswordButton" class="btn btn-main btn-enter"
                    type="submit">${enter}</button>
            <#if realm.resetPasswordAllowed>
                <span class="reset-password">
                            <a href="${url.loginResetCredentialsUrl}">${doForgotPassword}</a>
            </span>
            </#if>
        </#if>
        </div>
        </form>
        <form method="POST" action="${url.loginUrl}">
            <button name="off" type="submit">
                login via phone call
            </button>
        </form>
    </#if>
</#if>

<#if secondPhaseLogin!false>
<h3 class="verification__sub pb-2 sm:pb-3 md:pb-4" x-ms-format-detection="none">
<#--    Ur number:-->
<#--    <br/>-->
<#--    ${userPhone?replace('([0-9]{1})([0-9]{3})([0-9]{3})([0-9]{2})([0-9]{2})', '+$1 $2 $3 $4 $5', 'ri')}-->
    Введите код из СМС
    <form id="totpe" action="${url.loginAction}" method="POST">
    </form>
    <form id="totpForm" action="${url.loginAction}" method="POST">
        <p class="pb-2 sm:pb-3 md:pb-4 text-accentRed-1100"> ${error!}<p>
        <div class="flex justify-between w-full xl:pb-37px md:pb-10 sm:pb-8 pb-4">
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
        </div>
        <div class="sm:block md:flex w-full items-center text-center md:text-left">
            <button class="btn btn-main verification__btn verification__btn__accept w-full md:w-3/7 mr-0 md:mr-4"
                    name="accept" id="accept" type="submit">${doSubmit}</button>
        </div>
    </form>
    <form method="POST" action="${url.loginUrl}">
        <button name="on" type="submit">
            Войти с помощью логина
        </button>
    </form>
    </#if>


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
    </script>
    </@layout.registrationLayout>
