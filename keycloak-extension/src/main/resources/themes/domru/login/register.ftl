<#import "template.ftl" as layout>
<#import "templates/components.ftl" as components>
<#import "templates/blocks.ftl" as blocks>
<#import "util.ftl" as util>

<@layout.registrationLayout displayInfo=true displayCity=false; section >
    <#if section = "header">
        <@blocks.contentHeader mainTitle="${msg(registerTitle)}" secondaryTitle="${msg(doLogIn)}" secondaryHref="${url.loginUrl}" withBorder=true />
    <#elseif section = "form">
        <form id="registrationForm" action="${url.registrationAction}" method="post">
            <#-- В нашем случае firstName – это полное имя -->
            <@components.field class="mt-6 xl:mt-8 sm:mt-6 md:w-full" fieldName="firstName" value="${firstName!''}" label="${placeholderUsername}" placeholder="${placeholderUsername}" required=true style="display: none" />

            <#-- Пока бэк не уберет необходимость фамилии, скрою поле и отправлю дефис -->
            <@components.field class="mt-6 xl:mt-8 sm:mt-6 md:w-full" fieldName="lastName" value="-" label="Фамилия" placeholder="Фамилия" required=true style="display: none" />

            <@components.field
                class="md:w-full"
                fieldName="email"
                label="${util.if(isRegistrationFullTexts!false, fullPlaceholderEmail, placeholderEmail)}"
                placeholder="${util.if(isRegistrationFullTexts!false, fullPlaceholderEmail, placeholderEmail)}"
                required=true
                type="text"
                value="${lastLoginUsername!email!''}"
            />

            <#if !realm.registrationEmailAsUsername>
                <@components.field class="mt-6 xl:mt-8 sm:mt-6 md:w-full" fieldName="username" value="${username!''}" label="Имя пользователя" placeholder="Имя пользователя" required=true />
            </#if>

            <@components.field
                class="mt-6 xl:mt-8 sm:mt-6 md:w-full"
                fieldName="phone"
                label="${placeholderPhone}"
                placeholder="${placeholderPhone}"
                required=true
                value="${lastLoginPhone!phone!''}"
            />

            <#if passwordRequired??>
                <h3 class="pb-4 mt-8 text-black-80">Придумайте пароль</h3>

                <@blocks.password />
            </#if>

            <#if recaptchaRequired??>
                <div class="g-recaptcha w-full"
                     data-sitekey="${recaptchaSiteKey}"
                     data-callback="recaptchaCallback"
                     data-expired-callback="recaptchaExpiredCallback"
                     data-error-callback="recaptchaErrorCallback"
                >
                </div>
            </#if>

            <div class="flex page-buttons">
                <div class="flex flex-basis-auto items-center">
                    <button id="submit" class="btn btn-main reg-fields" type="submit">${msg(doRegister)}</button>
                </div>
                <a class="personal-data-process-accept flex flex-col justify-center items-left flex-basis-auto text-xs" href="https://moscow.b2b.dom.ru/agreement" target="_blink">
                    <span class="text-main-1000">
                        Нажимая кнопку, вы соглашаетесь <br>
                    </span>
                    <span class="reference reference_hoverable allowDoubleClick item_hover highlighted-text underline-hovering-text" style="font-weight: 350;">
                        с правилами обработки персональных данных
                    </span>
                </a>
            </div>
        </form>

        <#if realm.password && social.providers??>
            <div class="flex items-center social-providers">
                <div class="text-no-wrap text-with-login mr-6">${loginWith}</div>
                <ul class="logo-social-providers">
                    <#list social.providers as p>
                        <li class="mr-4">
                            <a href="${p.loginUrl}">
                                <div class="logo logo--${p.providerId}"></div>
                            </a>
                        </li>
                    </#list>
                </ul>
            </div>
        </#if>

        <script>
            var btn = document.getElementById("submit");
            btn.addEventListener('click', function () {
                submit()
            })

            function submit() {
                btn.style.pointerEvents = 'none';
            }
        </script>
    <#elseif section = "info" >
        <p>${twoStepAuthType}</p>
    <#--        <p>На указанный номер телефона будет выслано СМС с одноразовым паролем</p>-->
    </#if>
</@layout.registrationLayout>
