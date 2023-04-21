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
        <p class="">${loginTitleText}</p>
        <#if realm.password>
            <form id="loginForm" class="md:flex md:flex-wrap md:justify-between"
                  onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                <div class="field field__container field--required mt-6 xl:mt-8 sm:mt-6 md:w-full">
                    <input class="hidden w-0 h-0" id="domain-login" name="city">
                    <#if withCity?has_content && withCity == "TRUE">
                        <input id = "withCity" class="hidden w-0 h-0" name="withCity" value="TRUE">
                    </#if>
                    <#if showModal?has_content>
                        <input id = "showModalIframe" class="hidden w-0 h-0" name="showModal" value="${showModal}">
                    </#if>
                    <#if usernameEditDisabled??>
                        <input name="username" id="username" class="field__input" label="${usernameOrEmailPlaceholder}"
                               placeholder="${usernameOrEmailPlaceholder}" value="${(login.username!)}"
                               type="text" disabled/>
                    <#else>
                        <input name="username" id="username" class="field__input" label="${usernameOrEmailPlaceholder}"
                               placeholder="${usernameOrEmailPlaceholder}" value="${(login.username!)}"
                               type="text" autofocus autocomplete="off"/>
                    </#if>
                    <label class="field__label" for="username">${usernameOrEmailPlaceholder}</label>
                </div>

                <@components.field class="mb-6 mt-6 xl:mt-8 sm:mt-6 xl:mb-8 sm:mb-6 md:w-full" fieldName="password" label="${passwordPlaceholder}" placeholder="${passwordPlaceholder}" type="password" required=true />

                <div class="flex justify-between w-full items-center">
                    <button id="submit" class="btn btn-main btn-enter" type="submit">${enter}</button>
                    <#if realm.resetPasswordAllowed>
                        <span class="reset-password">
                            <a href="${url.loginResetCredentialsUrl}">${doForgotPassword}</a>
                        </span>
                    </#if>
                </div>

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
