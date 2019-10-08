<#import "template.ftl" as layout>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayInfo=true displayCity=false; section>
    <#if section = "header">
        <#include "templates/required-fields.html">
        
        <@blocks.contentHeader mainTitle="${msg('emailForgotContentTitle')}" />
    <#elseif section = "form">
        <form id="loginResetPasswordForm" action="${url.loginAction}" method="post">
            <div class="field field__container field--required mb-4 md:w-full">
                <input name="username" id="username" class="field__input" placeholder="${msg('usernameOrEmailPlaceholder')}" type="text" autofocus />
                <label class="field__label" for="username">
                    <#if !realm.loginWithEmailAllowed>
                        ${msg("username")}
                    <#elseif !realm.registrationEmailAsUsername>
                        ${msg("usernameOrEmail")}
                    <#else>
                        ${msg("email")}
                    </#if>
                </label>
            </div>

            <div class="flex flex-col justify-between sm:flex-row mt-10">
                <div class="flex-basis-1/2 mb-4 sm:mb-0">
                    <#--  <button id="submit" class="btn btn-main w-full" type="submit">${msg("doSubmit")}</button>  -->
                    <button id="submit" class="btn btn-main w-full" type="submit">Далее</button>
                </div>
                <div class="flex-basis-1/2 ml-0 text-sm sm:ml-6">
                    <#--  <button id="cancel" class="btn w-full" type="button">${msg("doDecline")}</button>  -->
                    <button id="cancel" class="btn w-full" type="button">Назад</button>
                </div>
            </div>
        </form>
    <#elseif section = "info" >
        ${msg("emailInstruction")}
    </#if>
</@layout.registrationLayout>
