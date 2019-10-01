<#import "template.ftl" as layout>
<#import "templates/components.ftl" as components>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout; section>
    <#if section = "header">
        ${msg("loginProfileTitle")}
    <#elseif section = "form">

        <form id="kc-update-profile-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
        <@components.field class="mb-4 md:w-full" fieldName="orgNameUP" label="Наименование организации" placeholder="Наименование организации" required=true />

    <#-- В нашем случае firstName – это полное имя -->
        <@components.field class="mb-4 md:w-full" fieldName="firstNameUP"value="${(user.firstName!'')}" label="Как к вам обращаться?" placeholder="Как к вам обращаться?" required=true />

    <#-- Пока бэк не уберет необходимость фамилии, скрою поле и отправлю дефис -->
        <@components.field class="mb-4 md:w-full" fieldName="lastNameUP" value="${(user.lastNameUP!'')}" label="Фамилия" placeholder="Фамилия" required=true style="display: none" />

        <@components.field class="mb-4 md:w-full" fieldName="emailUP" value="${(user.email!'')}" label="Эл. почта" placeholder="Ваш адрес эл.почты" required=true type="email" />

        <@components.field class="mb-4 md:w-full" fieldName="phoneUP" label="Ваш телефон" placeholder="+7 (XXX) XXX - XX - XX" required=true />


            <#if recaptchaRequired??>
                <div class="g-recaptcha w-full"
                data-size="compact"
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
                    <input class="btn btn-main w-full" type="submit" value="${msg('doRegister')}" />
                    <button class="btn btn-main w-full" type="submit" name="cancel-aia" value="true" />${msg("doCancel")}</button>
                    <#else>
                    <input class="btn btn-main w-full" type="submit" value="${msg('doRegister')}" />
                    </#if>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>
