<#macro defaultTemplate email="" backHref="/" buttonExist=true isVerified=false success = true>
  <div class="container">
    <div class="mr-33">
      <svg class="ok" viewBox="0 0 104 104" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path fill-rule="evenodd" clip-rule="evenodd" d="M52 96C76.3005 96 96 76.3005 96 52C96 27.6995 76.3005 8 52 8C27.6995 8 8 27.6995 8 52C8 76.3005 27.6995 96 52 96ZM52 104C80.7188 104 104 80.7188 104 52C104 23.2812 80.7188 0 52 0C23.2812 0 0 23.2812 0 52C0 80.7188 23.2812 104 52 104Z" fill="#69BE28"/>
        <path fill-rule="evenodd" clip-rule="evenodd" d="M76.082 41.6562L70.4258 36L47.7988 58.627L33.6562 44.4844L28 50.1406L42.1406 64.2852L47.7988 69.9414L53.4551 64.2832L76.082 41.6562Z" fill="#69BE28"/>
      </svg>
    </div>
    <div>
      <h1 class="text text__title"><#nested "header"></h1>
        <#if !isVerified>
          <p class="text text--far">
              <#nested "description">
          </p>
        </#if>
        <#if buttonExist>
            <#if success>
                <a class="btn btn-main text-center btn--thanks" href="${backHref}">Спасибо</a>
            <#else>
                <a class="btn btn-main text-center btn--thanks" href="${backHref}">На главную</a>
            </#if>
        </#if>
    </div>
  </div>
</#macro>