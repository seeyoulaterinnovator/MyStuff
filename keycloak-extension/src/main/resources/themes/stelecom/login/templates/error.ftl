<#macro codeAll backHref="/" backMessage="Попробовать снова" >
  <@allErrorTemplate heading="Что-то пошло не так" img="${url.resourcesPath}/build/images/error.png" backHref=backHref backMessage=backMessage >
    Регистрация временно недоступна, попробуйте повторить попытку позже
  </@allErrorTemplate>
</#macro>

<#macro allErrorTemplate heading img backHref backMessage>
  <img class="error-img" src="${img}" alt="" />
  <h1 class="mb-10">${heading}</h1>
  <p class="mb-8 text-sm">
    <#nested>
  </p>
  <a class="btn btn-main error-btn" href="${backHref}">Спасибо</a>
</#macro>
