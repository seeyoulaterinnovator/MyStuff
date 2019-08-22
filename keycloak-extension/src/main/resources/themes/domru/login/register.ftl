<#import "template.ftl" as layout>
<#import "templates/reload-button.ftl" as reloadButtonMacro>

<@layout.registrationLayout; section>
    <#if section = "header">
        <#include "templates/required-fields.html">
        <header class="flex justify-between items-center">
            <h1 id="page-title" class="border-extra border-b-2 md:border-b-3 xl:border-b-4">
                <b>
                    ${msg("registerTitle")}
                </b>
            </h1>

            <h2 class="text-main">
                <a href="${url.loginUrl}">
                    <b>
                        ${msg("doLogIn")}
                    </b>
                </a>
            </h2>
        </header>
    <#elseif section = "form">
        <p class="mt-3">
            На указанный номер телефона будет выслано СМС с одноразовым паролем
        </p>

        <form id="registrationForm" class="mb-4 pt-3" 
              action="${url.registrationAction}" method="post">
                <div class="field field--required mb-4 md:w-full">
                    <label class="field__label" for="orgName">Наименование организации</label>
                    <input class="field__input" name="orgName" id="organization" value="${(register.formData.orgName!'')}" placeholder="Наименование организации" type="text" />
                    <div class="field__error-message" id="orgName-error-message"></div>
                </div>

                <#-- В нашем случае firstName – это полное имя -->
                <div class="field field--required mb-4 md:w-full">
                    <label class="field__label" for="firstName">Как к вам обращаться?</label>
                    <input name="firstName" id="firstName" value="${(register.formData.firstName!'')}" placeholder="Как к вам обращаться?" class="field__input" type="text" />
                    <div class="field__error-message" id="firstName-error-message"></div>
                </div>

                <#-- Пока бэк не уберет необходимость фамилии, скрою поле и отправлю пробел -->
                <div class="field field--required mb-4 md:w-full" style="display: none">
                    <label class="field__label" for="lastName">Фамилия</label>
                    <input name="lastName" id="lastName" value="${(register.formData.lastName!'')}" placeholder="Фамилия" class="field__input" type="text" />
                    <div class="field__error-message" id="lastName-error-message"></div>
                </div>

                <div class="field field--required mb-4 md:w-full">
                    <label class="field__label" for="email" >Эл. почта</label>
                    <input name="email" id="email" value="${(register.formData.email!)}" placeholder="Ваш адрес эл.почты"  class="field__input" type="email" autocomplete="email" />
                    <div class="field__error-message" id="email-error-message"></div>
                </div>
                
                <#if !realm.registrationEmailAsUsername>
                    <div class="field field--required mb-4 md:w-full">
                        <label class="field__label" for="username" >Имя пользователя</label>
                        <input name="username" id="username" value="${(register.formData.username!)}" placeholder="Имя пользователя" class="field__input" autocomplete="username" />
                        <div class="field__error-message" id="username-error-message"></div>
                    </div>
                </#if>

                <div class="field field--required mb-4 md:w-full">
                    <label class="field__label" for="phone">Ваш телефон</label>
                    <input name="phone" id="phone" value="${(register.formData.phone!'')}" placeholder="+7 (XXX) XXX - XX - XX" class="field__input" type="text" />
                    <div class="field__error-message" id="phone-error-message"></div>
                </div>

                <#if passwordRequired>
                    <h3 class="py-4 text-black-80">Придумайте пароль</h3>
                    <p class="text-black-80">Пароль должен состоять из комбинации букв, цифр, cпецсимволов и быть не менее 8 и не более 16 символов</p>
                    
                    <div class="flex text-black-50 py-6">
                        <div id="letters-password" class="flex flex-1 flex-col mr-4">
                            <span class="text-xl">A-z</span>
                            <span class="text-sm hidden sm:block">Латинские символы с верхним и нижним регистром</span>
                        </div>
                        <div id="numbers-password" class="flex flex-1 flex-col mr-4">
                            <span class="text-xl">0–9</span>
                            <span class="text-sm hidden sm:block">Цифра или несколько цифр</span>
                        </div>
                        <div id="extraChars-password" class="flex flex-1 flex-col">
                            <span class="text-xl">_ ] [ - . ! #</span>
                            <span class="text-sm hidden sm:block">Возможные спецсимволы </span>
                        </div>
                    </div>

                    <div class="flex flex-col-reverse sm:flex-row">
                        <div class="sm:max-w-1/2">
                            <div class="field field--required mb-4">
                                <label class="field__label" for="password">${msg("password")}</label>
                                <input name="password" id="password" placeholder="${msg('passwordPlaceholder')}" class="field__input" type="password" autocomplete="new-password" />
                                <div class="field__error-message" id="password-error-message"></div>
                            </div>

                            <div class="field field--required mb-4">
                                <label class="field__label" for="password-confirm">${msg("passwordConfirm")}</label>
                                <input name="password-confirm" id="password-confirm" placeholder="${msg('passwordConfirmPlaceholder')}" class="field__input" type="password"/>
                                <div class="field__error-message" id="password-confirm-error-message"></div>
                            </div>
                        </div>

                        <div class="mx-auto sm:ml-5">
                            <button id="generate-password-button" type="button">
                                <span class="reference border-accentBlue text-accentBlue">Сгенерировать</span> 
                            </button>
                            <div id="generated-password-container" class=" hidden">
                                Не забудьте записать пароль
                                <div class="flex justify-between items-center">
                                    <div id="generated-password" class="flex"></div>
                                    <button id="refresh-password-button" class="w-12 h-12 focus:outline-none" type="button">
                                        <@reloadButtonMacro.svg color="accentBlue"></@reloadButtonMacro.svg>
                                    </button>
                                </div>                                
                            </div>
                        </div>
                    </div>

                    
                    
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
    </#if>
</@layout.registrationLayout>
