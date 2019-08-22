<#macro registrationLayout displayInfo=false displayMessage=true displayWide=false>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" lang="ru" class="h-full">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="robots" content="noindex, nofollow">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <#if properties.meta?has_content>
      <#list properties.meta?split(' ') as meta>
        <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
      </#list>
    </#if>
    <title>${msg("loginTitle",(realm.displayName!''))}</title>
    <link rel="icon" href="${url.resourcesPath}/assets/icons/1ok.png" />
    <#if properties.styles?has_content>
      <#list properties.styles?split(' ') as style>
        <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
      </#list>
    </#if>
  </head>
  <body class="flex flex-col h-full p-4 sm:px-6 md:py-6 lg:px-8 xl:py-8 xl:px-6">
    <#include "templates/header.html">

    <main id="content" class="flex-1 py-12 mx-auto md:mx-auto w-full max-w-md">
      <#nested "header">
      
      <div>
        <#if displayMessage && message?has_content>
          <div class="alert alert-${message.type}">
            <#if message.type = 'success'><span class="${properties.kcFeedbackSuccessIcon!}"></span></#if>
            <#if message.type = 'warning'><span class="${properties.kcFeedbackWarningIcon!}"></span></#if>
            <#if message.type = 'error'><span class="${properties.kcFeedbackErrorIcon!}"></span></#if>
            <#if message.type = 'info'><span class="${properties.kcFeedbackInfoIcon!}"></span></#if>
            <span>${kcSanitize(message.summary)?no_esc}</span>
          </div>
        </#if>

        <#nested "form">

        <#if displayInfo>
          <#nested "info">
        </#if>
      </div>
    </main>
    
    <#include "templates/footer-copyright.html">

    <div id="cities-modal"></div>

    <#if properties.scripts?has_content>
        <#list properties.scripts?split(' ') as script>
            <script src="${url.resourcesPath}/${script}" async></script>
        </#list>
    </#if>
    
    <#if scripts??>
        <#list scripts as script>
            <#if script?contains("recaptcha/api.js?")>
              <script src="${script}&onload=onloadRecaptchaCallback" async defer></script>
            <#elseif script?contains("recaptcha/api.js")>
              <script src="${script}?onload=onloadRecaptchaCallback" async defer></script>
            <#else>
              <script src="${script}" async defer></script>
            </#if>
        </#list>
    </#if>

  </body>
</html>
</#macro>
