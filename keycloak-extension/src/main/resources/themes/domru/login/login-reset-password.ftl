<#import "template.ftl" as layout>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayInfo=true; section>
    <#if section = "header">
        <#include "templates/required-fields.html">
        <@blocks.contentHeader mainTitle="${msg('emailForgotContentTitle')}" />
    <#elseif section = "form">
        <form id="loginResetPasswordForm" class="mb-4 pt-3" action="${url.loginAction}" method="post">
            <div class="field field--required mb-4 md:w-full">
                <label class="field__label" for="username">
                    <#if !realm.loginWithEmailAllowed>
                        ${msg("username")}
                    <#elseif !realm.registrationEmailAsUsername>
                        ${msg("usernameOrEmail")}
                    <#else>
                        ${msg("email")}
                    </#if>                
                </label>
                <input name="username" id="username" class="field__input" placeholder="${msg('usernameOrEmailPlaceholder')}" type="text" autofocus />
            </div>

            <button id="login" class="btn btn-main w-1/2" name="login" type="submit">${msg("doSubmit")}</button>
        </form>
    <#elseif section = "info" >
        ${msg("emailInstruction")}
    </#if>
</@layout.registrationLayout>
