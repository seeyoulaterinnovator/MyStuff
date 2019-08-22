<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=social.displayInfo displayWide=(realm.password && social.providers??); section>
    <#if section = "header">
        <#include "templates/required-fields.html">
        
        <header class="flex justify-between items-center">
            <h1 id="page-title" class="border-extra border-b-2 md:border-b-3 xl:border-b-4">
                <b>
                    ${msg("doLogIn")}
                </b>
            </h1>
            
            <#if realm.password && realm.registrationAllowed && !usernameEditDisabled??>
                <h2 class="text-main">
                    <a href="${url.registrationUrl}">
                        <b>
                            ${msg("doRegister")}
                        </b>
                    </a>
                </h2>
            </#if>
        </header>
    <#elseif section = "form">

        <p class="mt-3">
            Если у вас уже есть учетная запись, вы можете войти.
        </p>
         
        <#if realm.password>
            <form id="loginForm" class="mb-4 md:flex md:flex-wrap md:justify-between pt-3" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                <div class="field field--required mb-4 md:w-full">
                    <label class="field__label" for="username"><#if !realm.registrationEmailAsUsername>${msg("username")}</#if></label>
                    <#if usernameEditDisabled??>
                        <input name="username" id="username" class="field__input" placeholder="${msg('usernameOrEmailPlaceholder')}" value="${(login.username!)}" type="text" disabled />
                    <#else>
                        <input name="username" id="username" class="field__input" placeholder="${msg('usernameOrEmailPlaceholder')}" value="${(login.username!)}" type="text" autofocus autocomplete="off" />
                    </#if>
                </div>

                <div class="field field--required mb-6 md:w-full">
                    <label for="password" class="field__label">${msg("password")}</label>
                    <input name="password" id="password" class="field__input" placeholder="${msg('passwordPlaceholder')}" type="password" autocomplete="off" />
                </div>
                
                <div class="flex justify-between w-full items-center">
                    <button id="login" class="btn btn-main w-1/2" name="login" type="submit">Войти</button>
                    <#if realm.resetPasswordAllowed>
                        <span class="underline opacity-50 text-right"><a href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a></span>
                    </#if>
                </div>
                
            </form>
        </#if>
        <#if realm.password && social.providers??>
            <div class="flex items-center mt-8">
                <div class="mr-4">Войти через: </div>
                <ul>
                    <#list social.providers as p>
                        <li class="w-8 h-8">
                            <a href="${p.loginUrl}">
                                <div class="w-full h-full bg-contain bg-no-repeat logo logo--${p.providerId}"></div>
                            </a>
                        </li>
                    </#list>
                </ul>
            </div>
        </#if>
    </#if>

</@layout.registrationLayout>
