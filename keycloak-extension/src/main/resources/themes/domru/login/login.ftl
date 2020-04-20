<#import "template.ftl" as layout>
<#import "templates/components.ftl" as components>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayInfo=social.displayInfo displayWide=(realm.password && social.providers??); section>
    <#if section = "header">
        <#include "templates/required-fields.html">
        <@blocks.contentHeader mainTitle="${msg('doLogIn')}" secondaryTitle="${msg('registerTitle')}" secondaryHref="${url.registrationUrl}" withBorder=true />
    <#elseif section = "form">
        <p class="pb-3 login-title-text">Если у Вас уже есть учетная запись, Вы можете войти</p>
         
        <#if realm.password>
            <form id="loginForm" class="md:flex md:flex-wrap md:justify-between" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                <div class="field field__container field--required mb-3 sm:mb-4 md:w-full">
                  <input class="hidden w-0 h-0" id="domain-login" name="city">
                    <#if usernameEditDisabled??>
                        <input name="username" id="username" class="field__input" label="${msg('username')}" placeholder="${msg('usernameOrEmailPlaceholder')}" value="${(login.username!)}" type="text" disabled />
                    <#else>
                        <input name="username" id="username" class="field__input" label="${msg('username')}" placeholder="${msg('usernameOrEmailPlaceholder')}" value="${(login.username!)}" type="text" autofocus autocomplete="off" />
                    </#if>
                  <label class="field__label" for="username">${msg("username")}</label>
                </div>

                <@components.field class="mb-7 sm:mb-8 md:w-full" fieldName="password" label="${msg('password')}" placeholder="${msg('passwordPlaceholder')}" type="password" required=true />
                
                <div class="flex justify-between w-full items-center">
                    <button id="submit" class="btn btn-main w-1/2 btn-enter" type="submit">Войти</button>
                    <#if realm.resetPasswordAllowed>
                        <span class="reset-password text-right"><a href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a></span>
                    </#if>
                </div>
                
            </form>
        </#if>
        <#if realm.password && social.providers??>
            <div class="flex items-center mt-12">
                <div class="text-no-wrap mr-6">Войти через: </div>
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

</@layout.registrationLayout>

<script>

    window.addEventListener('message', function(event) {
        if (event.origin !== document.location.origin) {
            iframeInit();
        } else {
            return;
        }
    });
    function iframeInit() {
        console.log("iframe");
        var loginForm = document.getElementById('loginForm');
        var actionAttribute = loginForm.getAttribute("action");

        if (actionAttribute.indexOf("iframe") === -1) {
            actionAttribute = actionAttribute + "&iframe=1";
        }

        loginForm.setAttribute("action", actionAttribute);
    }

</script>
