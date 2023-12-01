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
        <#if !activateNewAuth || loginViaEmailOrUsernameAndPassword!true>
            <#if !activateNewAuth || !isSwitcherOn!true>
                <#if realm.password>
                    <form id="loginForm" class="md:flex md:flex-wrap md:justify-between"
                    onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                    <div class="field field__container field--required md:w-full">
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
                            <input name="username" id="username"
                                   class="field__input ${error?has_content?then('field__input--error','')}"
                                   label="${usernameOrEmailPlaceholder}"
                                   placeholder="Телефон или E-mail" value="${(login.username!)}"
                                   type="text" autofocus autocomplete="off"/>
                        </#if>
                        <label class="field__label" for="username">Телефон или E-mail</label>
                        <span class="span-line">${error!}</span>
                    </div>

                    <@components.field class="md:w-full mt-8" fieldName="password" label="Пароль" placeholder="Пароль" type="password" required=true />
<#--                    дернул с прода -->
<#--                       <div class="login-password-forgot">
                    <div class="login-pass mt-10">
                        <button id="submit" name="loginPasswordButton" class="btn btn-main btn-enter"
                                type="submit">${enter}</button>
                        <#if activateNewAuth!false>
                            <button id="topSecretButton" type="button" class="btn btn-switcher text-accentBlue-900"
                                    onclick="document.getElementById('smsLoginButton').click();">Получить временный код
                            </button>
                        </#if>
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
<#--                    изменил стиль на свой-->
<#--                    <div class="login-consent-password-forgot-code">-->
                    <div class="login-password-forgot">
                    <div class="login-pass mt-10">
<#--                    <div class="login-consent">-->
<#--                    <div class="flex flex-basis-auto items-center">-->
                        <button id="submit" name="loginPasswordButton" class="btn btn-main btn-enter"
                                type="submit">${enter}</button>
                        <#if activateNewAuth!false>
                            <span class="flex flex-col justify-center items-left flex-basis-auto text-xs sm:ml-5 sm:mt-0 mt-4">
<#--                        <span class="flex flex-col sm:flex-row mt-8">-->
                            <span class="opacity-50" style="font-weight: 350; color: #7585A1;">
                                 Нажимая кнопку, вы соглашаетесь <br>
                                </span>
                            <a class="reference reference_hoverable allowDoubleClick item_hover" style="font-weight: 350;"
                               href="https://moscow.b2b.dom.ru/agreement" target="_blink">
                                с Условиями обработки данных</a>
<#--                            </span>-->
                            </span>
                            </div>
                            <div class="flex justify-between code-forgot">
                            <a class="code" href="#" id="topSecretButton" onclick="document.getElementById('smsLoginButton').click();">Получить временный код</a>
                            <#if realm.resetPasswordAllowed>
                            <a class="reference reference_hoverable allowDoubleClick item_hover forgot" href="${url.loginResetCredentialsUrl}">${doForgotPassword}</a>
                            </#if>
                                </#if>
                    </div>
                </#if>
                </div>
                </form>
                <#if realm.password && social.providers??>
                    <div class="flex items-center mt-2">
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

                    <div class="field field__container field--required md:w-full">
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
                            <input name="username" id="username-second"
                                   class="field__input ${error?has_content?then('field__input--error','')}"
                                   label="${usernameOrEmailPlaceholder}"
                                   placeholder="Номер телефона" value="${(login.username!)}"
                                   type="text" autofocus autocomplete="off"/>

                        </#if>
                        <label class="field__label" for="username">Номер телефона</label>
                        <span class="span-line">${error!}</span>
                    </div>
                </#if>
                <div class="login-password-forgot">
                    <div class="login-pass mt-10">
                        <button id="submit-phone" name="smsButton" class="btn btn-main btn-enter btn-new-enter"
                                type="submit">${enter}123</button>
                        <button id="topSecretButton" type="button" class="w-full btn btn-back text-accentBlue-900"
                                onclick="document.getElementById('loginPasswordButton').click();">Войти с помощью логина
                        </button>
                    </div>
                </div>
                </form>
                <form id="off" method="POST" name="off" action="${url.loginUrl}">
                    <button id="loginPasswordButton" name="off" type="submit" class="hidden">
                    </button>
                </form>
            </#if>

        <#elseif loginViaSms!true>
            <#if !isSwitcherOn!true>
                <p class="mb-7">Мы отправим код в СМС</p>
                <#if realm.password>
                    <form id="loginForm" class="md:flex md:flex-wrap md:justify-between"
                    onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                    <div class="field field__container field--required md:w-full">
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
                            <input name="username" id="username-second"
                                   class="field__input ${error?has_content?then('field__input--error','')}"
                                   label="${usernameOrEmailPlaceholder}"
                                   placeholder="Номер телефона" value="${(login.username!)}"
                                   type="text" autofocus autocomplete="off"/>

                        </#if>
                        <label class="field__label" for="username">Номер телефона</label>
                        <span class="span-line">${error!}</span>
                    </div>
                </#if>
                <div class="login-password-forgot">
                    <div class="login-pass mt-10">
                        <button id="submit-phone" name="smsButton" class="btn btn-main btn-enter btn-new-enter"
                                type="submit">${enter}170</button>
                        <button id="topSecretButton" type="button" class="w-full btn btn-back text-accentBlue-900"
                                onclick="document.getElementById('loginPasswordButton').click();">Войти с помощью логина
                        </button>
                    </div>
                </div>

                </form>
                <form method="POST" action="${url.loginUrl}">
                    <button id="loginPasswordButton" name="on" type="submit" class="hidden">
                        Войти с помощью логина
                    </button>
                </form>

            <#elseif isSwitcherOn!false>
                <#if realm.password>
                    <form id="loginForm" class="md:flex md:flex-wrap md:justify-between"
                    onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                    <div class="field field__container field--required md:w-full">
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
                            <input name="username" id="username"
                                   class="field__input ${error?has_content?then('field__input--error','')}"
                                   label="${usernameOrEmailPlaceholder}"
                                   placeholder="Телефон или E-mail" value="${(login.username!)}"
                                   type="text" autofocus autocomplete="off"/>

                        </#if>
                        <label class="field__label" for="username">Телефон или E-mail</label>
                        <span class="span-line">${error!}</span>
                    </div>

                    <@components.field class="md:w-full mt-8" fieldName="password" label="Пароль" placeholder="Пароль" type="password" required=true />

                    <div class="login-password-forgot">
                    <div class="login-pass mt-10">
                        <button id="submit" name="loginPasswordButton" class="btn btn-main btn-enter"
                                type="submit">${enter}218</button>
                        <#if activateNewAuth!false>
                            <button id="topSecretButton" type="button" class="btn btn-switcher text-accentBlue-900"
                                    onclick="document.getElementById('smsLoginButton').click();">Получить временный код
                            </button>
                        </#if>
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
                <#if realm.password && social.providers??>
                    <div class="flex items-center mt-2">
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
                <form method="POST" name="smsLoginButton" action="${url.loginUrl}">
                    <button id="smsLoginButton" name="off" type="submit" class="hidden">
                        login via sms
                    </button>
                </form>
            </#if>

        <#elseif loginViaPhoneCall>
            <#if !isSwitcherOn!true>
                <p class="mb-7 info-text">На указанный номер поступит звонок. Для подтверждения <span class="breakable"> нужно ввести последние 4 цифры входящего номера</span>
                </p>
                <#if realm.password>
                    <form id="loginForm" class="md:flex md:flex-wrap md:justify-between"
                    onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">

                    <div class="field field__container field--required md:w-full">
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
                            <input name="username" id="username-second"
                                   class="field__input ${error?has_content?then('field__input--error','')}"
                                   label="${usernameOrEmailPlaceholder}"
                                   placeholder="Номер телефона" value="${(login.username!)}"
                                   type="text" autofocus autocomplete="off"/>

                        </#if>
                        <label class="field__label" for="username">Номер телефона</label>
                        <span class="span-line">${error!}</span>
                    </div>
                </#if>
                <div class="login-password-forgot">
                    <div class="login-pass mt-10">
                        <button id="submit-phone" name="phoneCallButton" class="btn btn-main btn-enter btn-new-enter"
                                type="submit">${enter}292</button>
                        <button id="topSecretButton" type="button" class="w-full btn btn-back text-accentBlue-900"
                                onclick="document.getElementById('loginPasswordButton').click();">Войти с помощью логина
                        </button>
                    </div>
                </div>
                </form>
                <form method="POST" action="${url.loginUrl}">
                    <button id="loginPasswordButton" name="on" type="submit" class="hidden">
                        Войти с помощью логина
                    </button>
                </form>

            <#elseif isSwitcherOn!false>
                <#if realm.password>
                    <form id="loginForm" class="md:flex md:flex-wrap md:justify-between"
                    onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                    <div class="field field__container field--required md:w-full">
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
                            <input name="username" id="username"
                                   class="field__input ${error?has_content?then('field__input--error','')}"
                                   label="${usernameOrEmailPlaceholder}"
                                   placeholder="Телефон или E-mail" value="${(login.username!)}"
                                   type="text" autofocus autocomplete="off"/>

                        </#if>
                        <label class="field__label" for="username">Телефон или E-mail</label>
                        <span class="span-line">${error!}</span>
                    </div>

                    <@components.field class="md:w-full mt-8" fieldName="password" label="Пароль" placeholder="Пароль" type="password" required=true />

                    <div class="login-password-forgot">
                    <div class="login-pass mt-10">
                        <button id="submit" name="loginPasswordButton" class="btn btn-main btn-enter"
                                type="submit">${enter}339</button>
                        <#if activateNewAuth!false>
                            <button id="topSecretButton" type="button" class="btn btn-switcher text-accentBlue-900"
                                    onclick="document.getElementById('smsLoginButton').click();">Получить временный код
                            </button>
                        </#if>
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
                <#if realm.password && social.providers??>
                    <div class="flex items-center mt-2">
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
                <form method="POST" action="${url.loginUrl}">
                    <button id="smsLoginButton" name="off" type="submit" class="hidden">
                        login via phone call
                    </button>
                </form>
            </#if>
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

        function submitForm1() {
            document.getElementById("on").submit();
        }
    </script>
</@layout.registrationLayout>
