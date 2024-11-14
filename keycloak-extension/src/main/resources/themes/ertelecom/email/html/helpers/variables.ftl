<#assign homePageSystemLinkInMessage = homePageSystem!"https://lk.ertelecom.ru">
<#assign homePageSystemTextInMessage = "ertelecom.ru">

<#assign phoneTextInMessage = phoneInMessage!"8 800 550 0479">
<#assign phoneLinkInMessage = phoneTextInMessage> 

<#--  TODO: Телефон с бэкенда приходит с элементом tel:...  -->
<#--  <#assign phoneLinkInMessage = "+78002500333"> 
<#if phoneConst?? && phoneConst?has_content>
  <#assign phoneLinkInMessage = phoneConst!"">
<#elseif phoneConstLink?? && phoneConstLink?has_content>
  <#assign phoneLinkInMessage = phoneConstLink!"">
</#if>  -->

<#assign systemName = "ЭР-Телеком Бизнес">

<#if email?? && email?has_content>
  <#assign email = email!"">
<#elseif user?? && user.getEmail??>
  <#assign email = user.getEmail()!email>
</#if>

<#assign extraLogin = login!email!"">
<#assign extraLoginAndPhone = login!email!phone!"">

<#assign textColor = "#000">
<#assign accentColor = "#000">
<#assign errorColor = "#E01D1D">
<#assign successColor = "#2C9F7D">
<#assign linkColor = "#4F4BFE">
<#assign linkHoverColor = "#4F4BFE">
