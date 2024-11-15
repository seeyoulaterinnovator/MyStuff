<#assign homePageSystemLinkInMessage = homePageSystem!"https://newlkb2b.dom.ru">
<#assign homePageSystemTextInMessage = "lkb2b.dom.ru">

<#assign phoneTextInMessage = phoneInMessage!"8 800 2500 333">
<#assign phoneLinkInMessage = phoneTextInMessage> 

<#--  TODO: Телефон с бэкенда приходит с элементом tel:...  -->
<#--  <#assign phoneLinkInMessage = "+78002500333"> 
<#if phoneConst?? && phoneConst?has_content>
  <#assign phoneLinkInMessage = phoneConst!"">
<#elseif phoneConstLink?? && phoneConstLink?has_content>
  <#assign phoneLinkInMessage = phoneConstLink!"">
</#if>  -->

<#assign systemName = "Дом.ру Бизнес">

<#if email?? && email?has_content>
  <#assign email = email!"">
<#elseif user?? && user.getEmail??>
  <#assign email = user.getEmail()!email>
</#if>

<#assign extraLogin = login!email!"">
<#assign extraLoginAndPhone = login!email!phone!"">

<#assign textColor = "#222222">
<#assign accentColor = "#FF312C">
<#assign errorColor = "#BD1B1D">
<#assign successColor = "#15A250">
<#assign linkColor = "#16629A">
<#assign linkHoverColor = "#0090D8">
