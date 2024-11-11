<#import "template.ftl" as template>
<#import "blocks.ftl" as blocks>

<@template.layout ; section>
    <#if section = "style">
    <#elseif section = "body">
        ${blockPrepareNotificationSchedulerHtml?no_esc}
        <@blocks.yourLogin login="${userName!}" phone="${phone!}"/>
        <@blocks.recoveryPasswordInstruction/>
    </#if>
</@template.layout>
