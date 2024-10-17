<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
    <#elseif section = "body">
        <div>
            <span style="font-size: 18px;   line-height: 24px;">
                ${kcSanitize(msg("emailCredentialDisableBodyHtml", authHref))?no_esc}
	        </span>
        </div>
    </#if>
</@template.layout>
