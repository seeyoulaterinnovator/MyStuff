<#include "./image.ftl" />

<#macro defaultTemplate backHref="/" >
  <div id="content" class="flex-1 py-8 md:py-12 mx-auto md:mx-auto w-full max-w-440px xl:max-w-470px">
    <div class="flex page-error-wrapper">
      <img id="big_cat" alt="" src="${base64ErrorImg}"/>
      <div>
        <div class="page-title-wrapper">
          <h1 class="page-title">
            <b class="titleAllPage">
              Что-то пошло не так
            </b>
          </h1>
        </div>
        <p>Регистрация временно недоступна, попробуйте повторить попытку позже.</p>
        <div class="page-buttons">
          <a href="${redirectUrl}" class="btn swr-button"><div>На главную</div></a>
        </div>
      </div>
    </div>
  </div>
</#macro>
