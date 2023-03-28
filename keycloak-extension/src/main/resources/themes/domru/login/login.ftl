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
            <#if !isSwitcherOn>
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
                    <button id="submit" class="btn btn-main btn-enter" type="submit">${enter}</button>
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

            <#elseif isSwitcherOn>
                <p class="mb-7">SMS will be sent to a provided phone number</p>
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
                    <button id="submit-phone" class="btn btn-main btn-enter" type="submit">${enter}</button>
                </div>
                </form>
                <form method="POST" action="${url.loginUrl}">
                    <button name="off" type="submit">
                        login via login/password
                    </button>
                </form>
            </#if>

        <#elseif loginViaSms!true>
            <#if !isSwitcherOn>
                <p class="mb-7">SMS will be sent to a provided phone number</p>
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
                    <button id="submit-phone" class="btn btn-main btn-enter" type="submit">${enter}</button>
                </div>
                </form>
                <form method="POST" action="${url.loginUrl}">
                    <button name="on" type="submit">
                        login via login/password
                    </button>
                </form>
            <#elseif isSwitcherOn>
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
                    <button id="submit" class="btn btn-main btn-enter" type="submit">${enter}</button>
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
                        login via sms
                    </button>
                </form>
            </#if>
        <#elseif loginViaPhoneCall>
            <#if !isSwitcherOn>
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
                    <button id="submit-phone" class="btn btn-main btn-enter" type="submit">${enter}</button>
                </div>
                </form>
                <form method="POST" action="${url.loginUrl}">
                    <button name="on" type="submit">
                        login via login/password
                    </button>
                </form>
            <#elseif isSwitcherOn>
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
                    <button id="submit" class="btn btn-main btn-enter" type="submit">${enter}</button>
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
