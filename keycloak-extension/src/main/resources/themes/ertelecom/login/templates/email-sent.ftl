<#macro defaultTemplate email="" backHref="/" buttonExist=true isVerified=false success = true>
  <div class="container">
    <div>
      <h1 class="text text__title"><#nested "header"></h1>
        <#if !isVerified>
          <p class="text text--far">
              <#nested "description">
          </p>
        </#if>
        <#if buttonExist>
            <#if success>
                <a class="btn btn-main text-center btn--thanks" href="${url.loginUrl}">Понятно</a>
            <#else>
                <a class="btn btn-main text-center btn--thanks" href="${url.loginUrl}">На главную</a>
            </#if>
        </#if>
    </div>
  </div>
</#macro>