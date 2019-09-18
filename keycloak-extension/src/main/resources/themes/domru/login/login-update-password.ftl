<#import "template.ftl" as layout>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayInfo=true displayCity=false displayWarningMessage=false; section>
    <#if section = "header">
        <#include "templates/required-fields.html">
        <@blocks.contentHeader mainTitle="${msg('emailForgotContentTitle')}" />
    <#elseif section = "form">
        <form id="loginUpdatePasswordForm" action="${url.loginAction}" method="post">
            <@blocks.password firstFieldName="password-new" />

            <button id="submit" class="btn btn-main pass-fields mt-6 pass-fields--change-pass" type="submit">Сменить пароль</button>
        </form>
    </#if>
</@layout.registrationLayout>