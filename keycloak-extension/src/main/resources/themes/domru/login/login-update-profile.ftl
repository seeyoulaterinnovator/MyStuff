<#import "template.ftl" as layout>
<#import "templates/components.ftl" as components>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout; section>
    <#if section = "header">
        <@blocks.contentHeader mainTitle="${msg(loginProfileTitle)}"/>

    <#elseif section = "form">

        <form id="kc-update-profile-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
    <#-- В нашем случае firstName – это полное имя -->
        <@components.field class="mt-6 xl:mt-8 sm:mt-6 md:w-full" fieldName="firstName"value="${(user.firstName!'')}" label="Как к Вам обращаться?" placeholder="Как к Вам обращаться?" required=true />

        <@components.field class="mt-6 xl:mt-8 sm:mt-6 md:w-full" fieldName="email" value="${(user.email!'')}" label="Эл. почта" placeholder="Введите эл. почту" required=true type="text" />

        <@components.field class="mt-6 xl:mt-8 sm:mt-6 md:w-full" fieldName="phone" label="Ваш телефон" placeholder="Введите номер телефона" required=true />


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
                        <input class="btn btn-main w-full mb-2" id="update-profile-submit" type="submit" value="${msg(doRegister)}" />
                        <button class="btn btn-main w-full" type="submit" name="cancel-aia" value="true" />${msg(doCancel)}</button>
                    <#else>
                        <div class="flex flex-col sm:flex-row mt-6 xl:mt-8 sm:mt-6">
                            <div class="flex flex-basis-auto items-center max-w-[40%]">
                                <button class="btn btn-main w-full" type="submit" id="update-profile-submit" />${msg(doAccept)}</button>
                            </div>
                            <span class="flex-basis-auto ml-2 mb-6 mt-4 text-xs sm:ml-5 sm:mb-0 agreement"><span class="opacity-50">Нажимая кнопку, вы соглашаетесь <br></span><a class="reference reference_hoverable allowDoubleClick item_hover" href="https://dom.ru/policy.pdf" target="_blink">с правилами обработки перс. данных</a></span>
                        </div>
                    </#if>
                </div>
            </div>
        </form>
    </#if>

    <script>
        var actionIsEmpty = ${actionIsEmpty?c};
        var clientIsB2B = ${clientIsB2B?c};

        if (clientIsB2B === true && actionIsEmpty === true) {
            window.onunload = function () {
                window.parent.postMessage('post-selected', '*');
                console.log("Отправлено тк B2B и Action пуст");
            };
        }
    </script>
</@layout.registrationLayout>
