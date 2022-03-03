<#import "template.ftl" as layout>
<#import "templates/email-sent.ftl" as emailSent>

<@layout.registrationLayout displayMessage=false; section>
    <#if section = "header">
        <#if messageHeader??>
            ${messageHeader}
        <#elseif message??>
            <#if message.summary != msg('accountUpdatedMessage')>
                ${message.summary}
            </#if>
        </#if>
    <#elseif section = "form">
        <div id="kc-info-message">
            <#if message??>
                <#if message.summary != msg('accountUpdatedMessage')>
                    <p class="instruction">${message.summary}<#if requiredActions??><#list requiredActions>: <b><#items as reqActionItem>${msg("requiredAction.${reqActionItem}")}<#sep>, </#items></b></#list><#else></#if></p>
                <#else>
                    <@emailSent.defaultTemplate buttonExist=false isVerified=true; section>
                        <#if section = "header">
                            Данные подтверждены
                        <#elseif section = "description">
                        </#if>
                    </@emailSent.defaultTemplate>
                </#if>
            </#if>
            <#if skipLink??>
            <#else>
                <#if pageRedirectUri??>
                    <#if iframe = true>
                        <p>Для окончания авторизации перезагрузите страницу</p>
                    <#else>
                        <p><a href="${pageRedirectUri}">${kcSanitize(msg(backToApplication))?no_esc}</a></p>
                    </#if>
                <#elseif actionUri??>
                    <p><a href="${actionUri}" id="action">${kcSanitize(msg(proceedWithAction))?no_esc}</a></p>
                    <script>
                        var url = window.location.href;
                        if (url.indexOf('tab_id') === -1 && url.indexOf('client_id') === -1) {
                            document.getElementById("action").click();
                        }
                    </script>
                <#elseif client.baseUrl??>
                    <#if iframe = true>
                        <p>Для окончания авторизации перезагрузите страницу</p>
                    <#else>
                        <p><a href="${client.baseUrl}">${kcSanitize(msg(backToApplication))?no_esc}</a></p>
                    </#if>
                </#if>
            </#if>
        </div>
    </#if>
</@layout.registrationLayout>