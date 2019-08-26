<#import "template.ftl" as layout>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        <@blocks.contentHeader mainTitle="Вам выслан одноразовый пароль на номер:" />
    <#elseif section = "form">
        <p class="pb-12">+7 999 00 000 00</p>
        
        <form id="totpForm" action="${url.loginAction}" method="POST">
            <div class="flex justify-between w-full pb-6">
                <#list 1..6 as x>
                    <input placeholder="-" type="number" maxlength="1" id="smscode-${x}" name="smscode-${x}" class="text-center align-middle text-3xl w-10 h-10 sm:w-16 sm:h-16 border rounded-lg focus:border-extra outline-none" autocomplete="off" />
                </#list>
            </div>

            <input id="smscode" name="smscode" class="hidden" />

            <div class="flex justify-between w-full items-center">
                <button class="btn btn-main w-3/7 mr-4" name="accept" id="accept" type="submit">Подтвердить</button>
                
                <div id="timer" class="text-main-600 text-right text-sm flex items-center">
                    Пароль действует <span id="timer-time" class="px-1 text-black text-5/3em"></span> мин
                </div>
                <button class="hidden border-b border-dashed text-black-50 text-right" name="resend" id="resend" type="submit" >Отправить еще раз</button>
            </div>
        </form>

        <#if client?? && client.baseUrl?has_content>
            <p><a id="backToApplication" href="${client.baseUrl}">${kcSanitize(msg("backToApplication"))?no_esc}</a></p>
        </#if>
    </#if>
</@layout.registrationLayout>