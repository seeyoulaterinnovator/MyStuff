<#import "./components.ftl" as components>
<#import "./svg.ftl" as svg>

<#macro password firstFieldName="password">
  <div id="password-block" class="mb-7 sm:mb-8">
    <p class="text-black">Пароль должен состоять из комбинации букв, цифр и быть не менее 8 и не более 16 символов</p>
    <div class="flex text-black-50 py-6">
      <div id="letters-password" class="flex flex-1 flex-col mr-6">
        <span class="text-symbols">A-z</span>
        <span class="text-sm sm:block">Латинские символы с верхним и нижним регистром</span>
      </div>
      <div id="numbers-password" class="flex flex-1 flex-col mr-4">
        <span class="text-symbols">0–9</span>
        <span class="text-sm sm:block">Цифра или несколько цифр</span>
      </div>
    </div>
    <p class="text-black-80 mb-8 mt-8">Не забудьте записать пароль <span id="generated-password" class="text-base"></span></p>
    <div class="flex sm:flex-row">
      <div class="pass-fields">
        <@components.field class="mb-3 sm:mb-4" fieldName="${firstFieldName}" label="${msg('passwordPlaceholder')}" placeholder="${msg('passwordPlaceholder')}" type="password" required=true value="" />
      </div>
      <div class="mx-auto generated-password btn-group flex">
        <button id="refresh-password-button" class="w-8 h-8 hidden-big focus:outline-none flex-child allowDoubleClick text-extra" type="button">
          <@svg.reloadButton color="accentBlue-1100"></@svg.reloadButton>
        </button>
        <button id="generate-password-button" type="button" class="generate-btn focus:outline-none pass--hidden-small flex-child allowDoubleClick">
          <span class="reference reference-generate-password border-accentBlue text-extra">ГЕНЕРИРОВАТЬ</span>
        </button>
      </div>

    </div>
  </div>
</#macro>

<#macro contentHeader mainTitle secondaryTitle="" secondaryHref="" withBorder=false >
  <header class="flex justify-between items-center sm:pb-3 md:pb-4">
    <h1
            id="page-title"
            class="<#if withBorder></#if> <#if secondaryTitle != ''>text-with-two</#if>"
    >
      <b <#if secondaryTitle == ''>class="titleAllPage"</#if>>
        ${mainTitle}
      </b>
    </h1>

    <#if secondaryTitle != "">
      <h2 class="text-secondary-title text-with-two">
        <a href="${secondaryHref}">
          <b>
            ${secondaryTitle}
          </b>
        </a>
      </h2>
    </#if>
  </header>
</#macro>

<#macro verificationHeader mainTitle>
  <header class="pb-2 sm:pb-3 md:pb-4">
    <h1
            id="page-title"
            class="verification__title"
    >
      ${mainTitle}
    </h1>
  </header>
</#macro>
