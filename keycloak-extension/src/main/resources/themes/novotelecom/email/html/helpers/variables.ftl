<#assign homePageSystemLinkInMessage = homePageSystem!"https://my.2090909.ru">
<#assign homePageSystemTextInMessage = "my.2090909.ru">

<#assign phoneTextInMessage = phoneInMessage!"209 09 09">
<#assign phoneLinkInMessage = "+73832090909"> 

<#--  TODO: Телефон с бэкенда приходит с элементом tel:...  -->
<#--  <#assign phoneLinkInMessage = "+78002500333"> 
<#if phoneConst?? && phoneConst?has_content>
  <#assign phoneLinkInMessage = phoneConst!"">
<#elseif phoneConstLink?? && phoneConstLink?has_content>
  <#assign phoneLinkInMessage = phoneConstLink!"">
</#if>  -->

<#assign systemName = "Электронный город Бизнес">

<#if email?? && email?has_content>
  <#assign email = email!"">
<#elseif user?? && user.getEmail??>
  <#assign email = user.getEmail()!email>
</#if>

<#assign extraLogin = login!email!"">
<#assign extraLoginAndPhone = login!email!phone!"">

<#assign textColor = "#001E35">
<#assign accentColor = "#FE403C">
<#assign errorColor = "#FE403C">
<#assign successColor = "#9BC722">
<#assign linkColor = "#1D58BF">
<#assign linkHoverColor = "#15418C">
