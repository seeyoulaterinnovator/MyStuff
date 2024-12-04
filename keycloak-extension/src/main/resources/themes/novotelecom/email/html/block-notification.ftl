<#import "template.ftl" as template>
<#import "./helpers/blocks.ftl" as blocks>

<#-- ??? way for getting -->
<@template.layout ; section>
  <#if section = "style">
    <title>Уведомление о блокировке</title>
  <#elseif section = "body">
    <@blocks.parameterizedMsg message=blockNotificationSchedulerHtml/>
  </#if>
</@template.layout>
