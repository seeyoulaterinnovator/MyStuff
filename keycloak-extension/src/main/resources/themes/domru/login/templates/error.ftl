<#macro code404 backHref="/" backMessage="На главную" >
  <@defaultErrorTemplate heading="Похоже, такой страницы не существует" img="${url.resourcesPath}/build/images/404.png" backHref=backHref backMessage=backMessage >
    Провайдер домашнего интернета, телевидения и телефона в Перми
    Дом.ru. Подождите немного и попробуйте обновить
    страницу еще раз, или перейдите на главную страницу
  </@defaultErrorTemplate>
</#macro>

<#macro code500 backHref="/" backMessage="Попробовать снова" >
  <@defaultErrorTemplate heading="Нет связи с сервером, но мы обязательно её восстановим" img="${url.resourcesPath}/build/images/500.png" backHref=backHref backMessage=backMessage >
    Провайдер домашнего интернета, телевидения и телефона в Перми Дом.ru. 
    Подождите немного и попробуйте обновить страницу еще раз, или перейдите
    <a href="${backHref}">на главную страницу</a>.
  </@defaultErrorTemplate>
</#macro>

<#macro defaultErrorTemplate heading img backHref backMessage>
  <div class="text-center">
    <h1 class="mb-10">${heading}</h1>
    <img class="mb-8" src="${img}" />
    <p class="mb-8 text-sm">
      <#nested>
    </p>
    <a class="btn btn-main mb-10" href="${backHref}">Попробовать снова</a>
  </div>
</#macro>