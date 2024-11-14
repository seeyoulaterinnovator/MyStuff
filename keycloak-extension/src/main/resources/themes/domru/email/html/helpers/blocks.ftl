<#include "variables.ftl" />

<#--  Параметриризированное сообщение 
все параметры передаются с целью унификации 
в теле каждого письма параметры будут доступны по соответствующему индексу  -->
<#function getParametrizedMsg message="">
    <#return msg(message,
        homePageSystemLinkInMessage,
        login!"",
        phone!"",
        email!"",
        code!"",
        time!"",
        expTime!"",
        expTimePass!"", 

        userName!"",
        userFirstName!"",
        userLastName!"",

        authHref!"", 
        link!"",
        accountLink!"",
        linkExpiration!"",

        requiredActionsText!"",
        identityProviderAlias!"",

        linkExpirationFormatterResult!"",
        usernameIdentityProviderContext!"",

        conditionalEmailLoginAndPhoneHtml!"", 
        filledEmailPasswordFooterHtml!"",

        phoneTextInMessage!"",
        extraLogin!"",
        systemName!"",
        phoneLinkInMessage!"",
        homePageSystemTextInMessage!"",

        textColor!"",
        accentColor!"",
        errorColor!"",
        successColor!"",
        linkColor!"",
        linkHoverColor!"",
        
        extraLoginAndPhone!""
    )
  >
</#function>

<#--  TODO: Не лучшее решение, т.к. в условиях идет привязака к конкретным полям, 
а в emailLoginAndPhoneHtml и emailLoginHtml могут использовать дургие параметры  -->
<#function getYourLoginBlock login="" phone="">
  <#local hasLogin = login?? && login?has_content>
  <#local hasPhone = phone?? && phone?has_content>
  
  <#if hasLogin && hasPhone>
    <#return getParametrizedMsg(emailLoginAndPhoneHtml)>
  <#elseif hasLogin || hasPhone>
    <#return getParametrizedMsg(emailLoginHtml)>
  </#if>

  <#return "">
</#function>

<#--  TODO: Исключить, т.к. испольузется в Additional Realm Settings - Message settings -->
<#macro yourLogin login="" phone="">
  <#local hasLogin = login?? && login?has_content>
  <#local hasPhone = phone?? && phone?has_content>

  <#if hasLogin || hasPhone>
    <div class="content__secondary content__secondary-mt">Ваш логин:</div>
  </#if>
  <#if hasLogin>
    <div style="font-size: 18px; font-weight: 700;">
      ${login}
    </div>
  </#if>
  <#if hasPhone>
    <#if hasLogin>
      <div class="content__secondary">или</div>
    </#if>
    <div style="font-size: 18px; font-weight: 700;">
      ${phone}
    </div>
  </#if>
</#macro>

<#--  TODO: Исключить, т.к. испольузется в Additional Realm Settings - Message settings -->
<#macro recoveryPasswordInstruction>
  <p style="font-size: 18px">
    Мы не храним ваши пароли.<br>
    Если вы забыли свой пароль или у вас не получается войти в Личный кабинет — воспользуйтесь формой
    восстановления пароля по ссылке <a class="no_block" href="${homePageSystemLinkInMessage?no_esc}">«Забыли пароль?»</a>.</p>
  <p>
  <div class="instruction">
    <div class="instruction_text">
      <div class="mark">&nbsp;</div>
      Для восстановления данных укажите ваш логин.
    </div>
    <div class="instruction_text">
      <div class="mark">&nbsp;</div>
      На ваш адрес электронной почты будет отправлена
      ссылка для восстановления пароля.<br>
      <span style="margin-left: 19px">Срок действия ссылки ${expTimePass?no_esc}.</span>
    </div>
  </div>
</#macro>

<#macro parameterizedMsg message="">
  <#assign conditionalEmailLoginAndPhoneHtml = getYourLoginBlock(extraLogin!, phone!)/>
  <#assign filledEmailPasswordFooterHtml = getParametrizedMsg(emailPasswordFooterHtml)/>

  <#if linkExpirationFormatter?? && linkExpirationFormatter?has_content && linkExpiration?? && linkExpiration?has_content>
      <#assign linkExpirationFormatterResult = linkExpirationFormatter(linkExpiration)!""/>
  </#if>

  <#if identityProviderContext?? && identityProviderContext?has_content && linkExpiration?? && linkExpiration?has_content>
      <#assign usernameIdentityProviderContext = identityProviderContext.username!""/>
  </#if> 

  ${kcSanitize(getParametrizedMsg(message))?no_esc}
</#macro>
