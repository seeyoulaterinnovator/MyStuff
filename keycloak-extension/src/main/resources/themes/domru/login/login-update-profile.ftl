<#import "template.ftl" as layout>
<#import "templates/components.ftl" as components>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout; section>
    <#if section = "header">
        <#include "templates/required-fields.html">
        <@blocks.contentHeader mainTitle="${msg('loginProfileTitle')}"/>

    <#elseif section = "form">

        <form id="kc-update-profile-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
    <#-- В нашем случае firstName – это полное имя -->
        <@components.field class="mb-4 md:w-full" fieldName="firstName"value="${(user.firstName!'')}" label="Как к Вам обращаться?" placeholder="Как к Вам обращаться?" required=true />

        <@components.field class="mb-3 sm:mb-4 md:w-full" fieldName="lastName" value="-" label="Фамилия" placeholder="Фамилия" required=true style="display: none" />

        <@components.field class="mb-4 md:w-full" fieldName="email" value="${(user.email!'')}" label="Эл. почта" placeholder="Ваш адрес эл.почты" required=true type="text" />

        <@components.field class="mb-4 md:w-full" fieldName="phone" label="Ваш телефон" placeholder="+7 (XXX) XXX - XX - XX" required=true />


            <#if recaptchaRequired??>
                <div class="g-recaptcha w-full mb-4"
                data-sitekey="${recaptchaSiteKey}"
                data-callback="recaptchaCallback"
                data-expired-callback="recaptchaExpiredCallback"
                data-error-callback="recaptchaErrorCallback"
                >
                </div>
            </#if>

            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-options" class="${properties.kcFormOptionsClass!}">
                    <div class="${properties.kcFormOptionsWrapperClass!}">
                    </div>
                </div>

                <div id="kc-form-buttons" class="${properties.kcFormButtonsClass!}">
                    <#if isAppInitiatedAction??>
                    <input class="btn btn-main w-full mb-2" id="update-profile-submit" type="submit" value="${msg('doRegister')}" />
                    <button class="btn btn-main w-full" type="submit" name="cancel-aia" value="true" />${msg("doCancel")}</button>
                    <#else>
                        <button class="btn btn-main pass-fields" type="submit" id="update-profile-submit" />${msg("doSubmit")}</button>
                    </#if>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>
