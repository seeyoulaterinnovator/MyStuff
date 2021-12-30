<#macro defaultTemplate email="" backHref="/" buttonExist=true isVerified=false>
  <div class="container">
    <div>
      <h1 class="text text__title"><#nested "header"></h1>
        <#if !isVerified>
          <p class="text text--far">
              <#nested "description">
          </p>
        </#if>
        <#if buttonExist>
          <a class="btn btn-main text-center btn--thanks" href="${backHref}">Спасибо</a>
        </#if>
    </div>
  </div>
</#macro>
