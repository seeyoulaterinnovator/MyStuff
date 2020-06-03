<#import "template.ftl" as layout>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayInfo=true displayCity=false displayWarningMessage=false; section>
    <#if section = "header">
        <#include "templates/required-fields.html">
        <@blocks.contentHeader mainTitle="${msg('emailForgotContentTitle')}" />
    <#elseif section = "form">
        <form id="loginUpdatePasswordForm" action="${url.loginAction}" method="post">
            <@blocks.password firstFieldName="password-new" />

            <div class="flex flex-col justify-between sm:flex-row xl:mt-12 md:mt-8 mt-7">
                <div class="flex-basis-auto w-full mb-2 sm:mb-0">
                    <button id="submit" class="btn btn-main w-full pass-fields pass-fields--change-pass" type="submit">Сменить пароль</button>
                </div>
                <div class="flex-basis-auto w-full ml-0 text-sm sm:ml-6">
                    <a id="cancel" href="https://master.frontend2.b2bweb.t2.ertelecom.ru" class="btn w-full">${msg("doCancel")}</a>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>
