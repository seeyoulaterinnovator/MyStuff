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
    <p class="text-black mb-8 mt-8">Не забудьте записать пароль <span id="generated-password" class="text-base"></span></p>
    <div class="flex sm:flex-row">
      <div class="pass-fields">
        <@components.field class="mb-3 sm:mb-4" fieldName="${firstFieldName}" label="${msg('password')}" placeholder="${msg(passwordPlaceholder)}" type="password" required=true value="" />
      </div>
      <div class="mx-auto generated-password generated_hover btn-group flex">
        <button id="refresh-password-button" class="w-8 h-12 focus:outline-none flex-child allowDoubleClick" type="button">
          <svg width=16 height=16 viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg" style="display: initial;" class="fill-current text-accentBlue-1100" id="refresh-password-loader">
            <path fill-rule="evenodd" clip-rule="evenodd" d="M23.3164 3.44824L19.8115 4.61719C24.5703 6.20996 28 10.7041 28 15.999C28 20.6641 25.3379 24.7051 21.4551 26.6895L20.5449 24.9092C23.7852 23.2529 26 19.8838 26 15.999C26 11.5215 23.0566 7.73145 19 6.45703L18.8369 6.30859L20.4229 10.1152L18.5771 10.8838L15.6631 3.89062L22.6836 1.55078L23.3164 3.44824ZM11.4551 7.08887C8.21484 8.74512 6 12.1143 6 15.999C6 20.1709 8.55566 23.7471 12.1865 25.2461L10.5771 21.3838L12.4229 20.6152L15.3369 27.6084L8.31641 29.9482L7.68359 28.0508L11.0439 26.9307C6.88965 25.0439 4 20.8594 4 15.999C4 11.334 6.66211 7.29297 10.5449 5.30762L11.4551 7.08887Z"/>
          </svg>
        </button>
        <button id="generate-password-button" type="button" class="generate-btn focus:outline-none pass--hidden-small flex-child allowDoubleClick">
          <span class="reference reference-generate-password border-accentBlue text-accentBlue-1100">ГЕНЕРИРОВАТЬ</span>
        </button>
      </div>

    </div>
  </div>
</#macro>

<#macro contentHeader mainTitle secondaryTitle="" secondaryHref="" withBorder=false >
  <header class="flex justify-between items-center pb-2 sm:pb-3 md:pb-4">
    <h1
            id="page-title"
            class="<#if withBorder></#if> <#if secondaryTitle != ''>text-3xl</#if>"
    >
      <b <#if secondaryTitle == ''>class="titleAllPage"</#if>>
        ${mainTitle}
      </b>
    </h1>

    <#if secondaryTitle != "">
      <h2 class="text-secondary-title text-3xl">
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
