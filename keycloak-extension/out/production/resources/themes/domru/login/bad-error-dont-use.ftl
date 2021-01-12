<#--  эта страница должна быть со сломанной версткой  -->

<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "header">
        ${msg("someTitle")}
    <#elseif section = "form">
        <form id="kc-register-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
        <button type="submit" class="btn btn-main w-full mb-4" name="submitAction" id="id1" value="Button">${msg("Button")}</button>
        <button type="submit" class="btn btn-main w-full" name="submitAction" id="id2" value="Another button">${msg("Another button")}</button>
        </form>
    </#if>
    <#if thisVariableNotExists>
        bad layout
    </#if>

</@layout.registrationLayout>