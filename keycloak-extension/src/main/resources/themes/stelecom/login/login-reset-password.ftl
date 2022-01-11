<#import "template.ftl" as layout>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayInfo=true displayCity=true; section>
    <#if section = "header">
        <#include "templates/required-fields.html">

        <@blocks.contentHeader mainTitle="${msg('emailForgotContentTitle')}" />
    <#elseif section = "form">
        <form id="loginResetPasswordForm" action="${url.loginAction}" method="post">
            <input name="city" id="city" class="city_hidden_input" type="text" />
            <div class="field field__container field--required mb-4 md:w-full">
                <input name="username" id="username" class="field__input" placeholder="${msg('usernameOrEmailPlaceholder')}" type="text" autofocus />
                <label class="field__label" for="username">${msg('usernameOrEmailPlaceholder')}</label>
            </div>

            <div class="flex flex-col justify-between sm:flex-row xl:mt-12 md:mt-8 mt-7">
                <div class="flex-basis-auto w-full mb-2 sm:mb-0">
                    <#--  <button id="submit" class="btn btn-main w-full" type="submit">${msg("doSubmit")}</button>  -->
                    <button id="submit" class="btn btn-main w-full" type="submit">Далее</button>
                </div>
                <div class="flex-basis-auto w-full ml-0 text-sm sm:ml-6">
                    <#--  <button id="cancel" class="btn w-full" type="button">${msg("doCancel")}</button>  -->
                    <a id="cancel" href="${redirectUrl}" class="btn btn-cancel w-full">${msg("doCancel")}</a>
                </div>
            </div>
        </form>
    <#elseif section = "info" >
        ${msg("emailInstruction")}
    </#if>
</@layout.registrationLayout>
