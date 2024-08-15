<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
    <#elseif section = "body">
        ${passwordExpiresSchedulerHtml?no_esc}
    </#if>
</@template.layout>
