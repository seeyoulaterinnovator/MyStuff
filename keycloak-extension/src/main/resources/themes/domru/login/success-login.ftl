<#import "template.ftl" as layout>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayMessage=false displayCity=false; section>
    <#if section = "form">
        <h3 class="verification__sub pb-2 sm:pb-3 md:pb-4">
            Вы успешно залогинились.<br/>Для окончания авторизации перезагрузите страницу
        </h3>
    </#if>
</@layout.registrationLayout>
