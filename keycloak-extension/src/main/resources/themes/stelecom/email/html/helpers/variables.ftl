<#assign homePageSystemLinkInMessage = homePageSystem!"https://lkb2b.tcenter.ru">
<#assign homePageSystemTextInMessage = "lkb2b.tcenter.ru">

<#assign phoneTextInMessage = phoneInMessage!"8 (495) 145 9 555">
<#assign phoneLinkInMessage = phoneTextInMessage> 

<#--  TODO: Телефон с бэкенда приходит с элементом tel:...  -->
<#--  <#assign phoneLinkInMessage = "+78002500333"> 
<#if phoneConst?? && phoneConst?has_content>
  <#assign phoneLinkInMessage = phoneConst!"">
<#elseif phoneConstLink?? && phoneConstLink?has_content>
  <#assign phoneLinkInMessage = phoneConstLink!"">
</#if>  -->

<#assign systemName = "Телеком Центр">

<#if email?? && email?has_content>
  <#assign email = email!"">
<#elseif user?? && user.getEmail??>
  <#assign email = user.getEmail()!email>
</#if>

<#assign extraLogin = login!email!"">
<#assign extraLoginAndPhone = login!email!phone!"">

<#assign textColor = "#000">
<#assign accentColor = "#0CB779">
<#assign errorColor = "#FF372B">
<#assign successColor = "#2AC08A">
<#assign linkColor = "#007BFB">
<#assign linkHoverColor = "#0FC8F9">
