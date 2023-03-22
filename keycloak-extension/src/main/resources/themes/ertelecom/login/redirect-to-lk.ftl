<#import "template.ftl" as layout>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayInfo=true displayCity=true; section>
    <#if section = "header">
        <@blocks.contentHeader mainTitle="${msg('loginToB2B')}" />
    <#elseif section = "form">
        <form id="loginResetPasswordForm" action="${redirectTo}" method="post" target="_top">
            <div class="flex flex-col justify-between sm:flex-row xl:mt-12 md:mt-8 mt-7">
                <div class="flex-basis-auto w-full mb-2 sm:mb-0">
                    <button id="submit" class="btn btn-main w-full" type="submit">Войти</button>
                    <input name="btoken" value="${redirectHeader}" type="hidden"/>
                </div>
                <div class="flex-basis-auto w-full ml-0 text-sm sm:ml-6">
                    <a id="cancel" href="https://b2b.dom.ru" class="btn w-full" target="_top">Вернуться на главную</a>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>
