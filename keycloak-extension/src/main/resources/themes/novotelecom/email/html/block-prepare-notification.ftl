<#import "template.ftl" as template>
<#import "./helpers/blocks.ftl" as blocks>

<#-- ??? way for getting -->
<#--  not used  -->
<@template.layout ; section>
    <#if section = "style">
      <title>Уведомление перед блокировкой</title>
    <#elseif section = "body">
        ${blockPrepareNotificationSchedulerHtml?no_esc}
        <@blocks.yourLogin login="${userName!}" phone="${phone!}"/>
        <@blocks.recoveryPasswordInstruction/>
    </#if>
</@template.layout>
