<#import "template.ftl" as layout>
<#import "templates/components.ftl" as components>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayInfo=true; section>
    <#if section = "header">
        <#include "templates/required-fields.html">
        <@blocks.contentHeader mainTitle="${msg('registerTitle')}" secondaryTitle="${msg('doLogIn')}" secondaryHref="${url.loginUrl}" withBorder=true />
    <#elseif section = "form">
        <form id="registrationForm" action="${url.registrationAction}" method="post">
                <@components.field class="mb-4 md:w-full" fieldName="orgName" label="Наименование организации" placeholder="Наименование организации" required=true />
                
                <#-- В нашем случае firstName – это полное имя -->
                <@components.field class="mb-4 md:w-full" fieldName="firstName" label="Как к вам обращаться?" placeholder="Как к вам обращаться?" required=true />

                <#-- Пока бэк не уберет необходимость фамилии, скрою поле и отправлю дефис -->
                <@components.field class="mb-4 md:w-full" fieldName="lastName" label="Фамилия" placeholder="Фамилия" required=true style="display: none" />

                <@components.field class="mb-4 md:w-full" fieldName="email" label="Эл. почта" placeholder="Ваш адрес эл.почты" required=true type="email" />
                
                <#if !realm.registrationEmailAsUsername>
                    <@components.field class="mb-4 md:w-full" fieldName="username" label="Имя пользователя" placeholder="Имя пользователя" required=true />
                </#if>

                <@components.field class="mb-4 md:w-full" fieldName="phone" label="Ваш телефон" placeholder="+7 (XXX) XXX - XX - XX" required=true />

                
                <#if passwordRequired>
                    <h3 class="py-4 text-black-80">Придумайте пароль</h3>
                    
                    <@blocks.password />
                </#if>

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

                <div class="flex justify-between">
                    <div class="flex flex-basis-1/2 items-center">
                        <button id="submit" class="btn btn-main w-full" type="submit">${msg('doRegister')}</button>    
                    </div>                       
                    <span class="flex-basis-1/2 ml-5 text-sm">Нажимая кнопку вы соглашаетесь <a class="reference" href="https://domru.ru/policy.pdf" target="_blink">с политикой обработки данных</a></span>
                </div>
        </form>
    <#elseif section = "info" >
        <p>На указанный номер телефона будет выслано СМС с одноразовым паролем</p>
    </#if>
</@layout.registrationLayout>
