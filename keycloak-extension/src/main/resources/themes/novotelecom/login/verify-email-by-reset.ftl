<#import "template.ftl" as layout>
<#import "templates/components.ftl" as components>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayInfo=true displayCity=true; section>
    <#if section = "header">

        <@blocks.contentHeader mainTitle="${msg(emailForgotContentTitle)}" />
    <#elseif section = "form">
        <form id="verifyEmailByResetForm" action="${url.loginAction}" method="post">
            <div class="field field__container field--required mb-4 md:w-full">
                <input name="verifyEmail" id="verifyEmail" class="field__input"
                       label="${placeholderEmail}" placeholder="${placeholderEmail}" value="${email!''}"
                       required=true type="text"/>
            </div>
            <div class="flex flex-col justify-between sm:flex-row xl:mt-12 md:mt-8 mt-7">
                <div class="flex-basis-auto w-full mb-2 sm:mb-0">
                    <#--  <button id="submit" class="btn btn-main w-full" type="submit">${msg("doSubmit")}</button>  -->
                    <button id="submit" class="btn btn-main w-full" type="submit">Подтвердить почту</button>
                </div>
                <div class="flex-basis-auto w-full ml-0 text-sm sm:ml-6">
                    <#--  <button id="cancel" class="btn w-full" type="button">${msg("doCancel")}</button>  -->
                    <a id="cancel" href="${url.loginUrl}" class="btn btn-cancel w-full domru-cancel">${msg(doCancel)}</a>
                </div>
            </div>
        </form>
    <#elseif section = "info" >
        <p class="mb-7"> Укажите ваш Email заданный при регистрации на нашем сайте.</p>
    </#if>
</@layout.registrationLayout>