<#ftl output_format="plainText">

<#assign requiredActionsText>
  <#if requiredActions??>
    <#list requiredActions>
      <#items as reqActionItem>
        ${msg("requiredAction.${reqActionItem}")}<#sep>, 
      </#items>
    </#list>
  <#else>
  </#if>
</#assign>


<#if requiredActions??>
    <#list requiredActions>
        <#items as reqActionItem>
            <#if reqActionItem == 'phone_verificator_sms'>
                ${msg("executeActionsConfirmPhoneBody", link, linkExpiration, realmName, requiredActionsText, linkExpirationFormatter(linkExpiration))}
            <#elseif reqActionItem == 'incoming_call_phone_verificator'>
                ${msg("executeActionsConfirmPhoneBody", link, linkExpiration, realmName, requiredActionsText, linkExpirationFormatter(linkExpiration))}
            <#elseif reqActionItem == 'VERIFY_EMAIL'>
                ${msg("emailVerificationBody", link, linkExpiration, realmName, requiredActionsText, linkExpirationFormatter(linkExpiration))}
            <#else>
                ${msg("executeActionsBody", link, linkExpiration, realmName, requiredActionsText, linkExpirationFormatter(linkExpiration))}
            </#if>
        </#items>
    </#list>
</#if>
${msg("executeActionsBody",link, linkExpiration, realmName, requiredActionsText, linkExpirationFormatter(linkExpiration))}