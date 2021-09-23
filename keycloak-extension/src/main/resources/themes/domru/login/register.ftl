<#import "template.ftl" as layout>
<#import "templates/components.ftl" as components>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayInfo=true displayCity=false; section >
    <#if section = "header">
        <#include "templates/required-fields.html">
        <@blocks.contentHeader mainTitle="${msg('registerTitle')}" secondaryTitle="${msg('doLogIn')}" secondaryHref="${url.loginUrl}" withBorder=true />
    <#elseif section = "form">
        <form id="registrationForm" action="${url.registrationAction}" method="post">
                <@components.field class="mb-3 md:w-full" fieldName="orgName" value="${orgName!''}" label="Наименование организации" placeholder="Наименование организации" required=true />

                <#-- В нашем случае firstName – это полное имя -->
                <@components.field class="mb-3 sm:mb-4 md:w-full" fieldName="firstName" value="${firstName!''}" label="Как к Вам обращаться?" placeholder="Как к Вам обращаться?" required=true />

                <#-- Пока бэк не уберет необходимость фамилии, скрою поле и отправлю дефис -->
                <@components.field class="mb-3 sm:mb-4 md:w-full" fieldName="lastName" value="-" label="Фамилия" placeholder="Фамилия" required=true style="display: none" />

                <@components.field class="mb-3 sm:mb-4 md:w-full" fieldName="email" value="${email!''}" label="Эл. почта" placeholder="Ваш адрес эл.почты" required=true type="text" />

                <#if !realm.registrationEmailAsUsername>
                    <@components.field class="mb-3 sm:mb-4 md:w-full" fieldName="username" value="${username!''}" label="Имя пользователя" placeholder="Имя пользователя" required=true />
                </#if>

                <@components.field class="mb-3 sm:mb-4 md:w-full" fieldName="phone" value="${phone!''}" label="Телефон" placeholder="+7 (XXX) XXX - XX - XX" required=true />


                <#if passwordRequired>
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

                <div class="flex flex-col-reverse justify-between sm:flex-row mt-0 xl:mt-2">
                  <div class="flex flex-basis-auto items-center">
                    <button id="submit" class="btn btn-main pass-fields" type="submit">${msg('doRegister')}</button>
                  </div>
                  <span class="flex-basis-auto ml-0 mb-6 sm:text-sm text-xs sm:ml-5 sm:mb-0"><span class="opacity-50">Нажимая кнопку, Вы соглашаетесь </span><a class="reference reference_hoverable" href="https://domru.ru/policy.pdf" target="_blink">с политикой обработки данных</a></span>
                </div>
        </form>

        <script>
            var btn = document.getElementById("submit");
            btn.addEventListener('click',function (){
                submit()
            })
            function submit() {
                btn.style.pointerEvents='none';
            }
        </script>
    <#elseif section = "info" >
        <p>${twoStepAuthType}</p>
<#--        <p>На указанный номер телефона будет выслано СМС с одноразовым паролем</p>-->
    </#if>
</@layout.registrationLayout>
