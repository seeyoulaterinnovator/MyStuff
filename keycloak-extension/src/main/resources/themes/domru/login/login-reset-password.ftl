<#import "template.ftl" as layout>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayInfo=true displayCity=true; section>
    <#if section = "header">

        <@blocks.contentHeader mainTitle="${msg(emailForgotContentTitle)}" />
    <#elseif section = "form">
        <form id="loginResetPasswordForm" action="${url.loginAction}" method="post">
            <input name="city" id="city" class="city_hidden_input" type="text" />
            <div class="field field__container field--required mb-4 md:w-full">
                <input name="username" id="username" class="field__input" placeholder="${msg(usernameOrEmailPlaceholder)}" type="text" autofocus />
                <label class="field__label" for="username">
                    <#if !realm.loginWithEmailAllowed>
                        ${msg(username)}
                    <#elseif !realm.registrationEmailAsUsername>
                        ${msg(usernameOrEmail)}
                    <#else>
                        ${msg(phoneOrEmail)}
                    </#if>
                </label>
            </div>

            <div class="sm:flex sm:flex-row page-buttons">
                <div class="mb-2 sm:mb-0">
                    <#--  <button id="submit" class="btn btn-main w-full" type="submit">${msg("doSubmit")}</button>  -->
                    <button id="submit" class="btn btn-main w-full" type="submit">${msg(restoreButtonLabel!next)}</button>
                </div>
                <div class="ml-0 text-sm sm:ml-6">
                    <#--  <button id="cancel" class="btn w-full" type="button">${msg("doCancel")}</button>  -->
                    <a id="cancel" href="${url.loginUrl}" class="btn btn-cancel text-accentBlue-900 w-full domru-cancel">${msg(doCancel)}</a>
                </div>
            </div>
        </form>
    <#elseif section = "info" >
        <p class="mb-10"> ${msg(emailInstruction)}</p>
    </#if>
</@layout.registrationLayout>
