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

            <#--  <button id="submit" class="btn btn-main w-1/2" type="submit">${msg("doSubmit")}</button>  -->
            <button id="submit" class="btn btn-main w-1/2 mt-10" type="submit">Далее</button>
        </form>
    <#elseif section = "info" >
        ${msg("emailInstruction")}
    </#if>
</@layout.registrationLayout>
