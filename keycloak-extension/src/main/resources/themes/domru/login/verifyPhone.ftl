<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "header">
        ${msg("termsTitle")}
    <#elseif section = "form">
        <div id="kc-terms-text">
        ${kcSanitize(msg("termsText"))?no_esc}
        </div>
        <form class="form-actions" action="${url.loginAction}" method="POST">
    <div class="${properties.kcFormGroupClass!}">
    <label for="smscode" class="${properties.kcLabelClass!}">SMS CODE:</label>

    <input tabindex="1" id="smscode" class="${properties.kcInputClass!}" name="smscode" value="" type="text" autofocus autocomplete="off" />
        </div>
    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="accept" id="kc-accept" type="submit" value="Отправить"/>
    <input class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonLargeClass!}" name="resend" id="kc-resend" type="submit" value="Повторить"/>
        </form>
        <div class="clearfix"></div>
    </#if>
</@layout.registrationLayout>