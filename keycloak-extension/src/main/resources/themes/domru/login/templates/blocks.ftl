<#import "./components.ftl" as components>
<#import "./svg.ftl" as svg>

<#macro password firstFieldName="password" secondFieldName="password-confirm" >
  <div id="password-block" class="mb-7 sm:mb-8">
    <p class="text-black-80">Пароль должен состоять из комбинации букв, цифр, cпецсимволов и быть не менее 8 и не более 16 символов</p>
    <div class="flex text-black-50 py-6">
      <div id="letters-password" class="flex flex-1 flex-col mr-4">
        <span class="text-symbols">A-z</span>
        <span class="text-sm hidden sm:block">Латинские символы с верхним и нижним регистром</span>
      </div>
      <div id="numbers-password" class="flex flex-1 flex-col mr-4">
        <span class="text-symbols">0–9</span>
        <span class="text-sm hidden sm:block">Цифра или несколько цифр</span>
      </div>
      <div id="extraChars-password" class="flex flex-1 flex-col">
        <span class="text-symbols">_ ] [ - . ! #</span>
        <span class="text-sm hidden sm:block">Возможные спецсимволы </span>
      </div>
    </div>

    <div class="flex flex-col-reverse sm:flex-row">
      <div class="pass-fields">
        <@components.field class="mb-3 sm:mb-4" fieldName="${firstFieldName}" label="${msg('password')}" placeholder="${msg('passwordPlaceholder')}" type="password" required=true />
        
        <@components.field fieldName="${secondFieldName}" label="${msg('passwordConfirm')}" placeholder="${msg('passwordConfirmPlaceholder')}" type="password" required=true />
      </div>

      <div class="mx-auto generated-password">
        <button id="generate-password-button" type="button" class="generate-btn">
          <span class="reference reference-generate-password border-accentBlue text-accentBlue">Сгенерировать</span>
        </button>
        <div id="generated-password-container" class=" hidden">
          <p class="generated-password-text">Не забудьте записать пароль</p>
          <div class="flex justify-between items-center">
            <div id="generated-password" class="flex text-base"></div>
            <button id="refresh-password-button" class="w-12 h-12 focus:outline-none" type="button">
              <@svg.reloadButton color="accentBlue"></@svg.reloadButton>
            </button>
          </div>                                
        </div>
      </div>
    </div>
  </div>
</#macro>

<#macro contentHeader mainTitle secondaryTitle="" secondaryHref="" withBorder=false >
  <header class="flex justify-between items-center pb-2 sm:pb-3 md:pb-4">
    <h1 
      id="page-title" 
      class="<#if withBorder>border-extra border-b-2 md:border-b-3 xl:border-b-4</#if> <#if secondaryTitle != ''>text-3xl</#if>"
    >
      <b>
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
  <header class="flex justify-between items-center pb-2 sm:pb-3 md:pb-4">
    <h1
      id="page-title"
      class="verification__title"
    >
      ${mainTitle}
    </h1>
  </header>
</#macro>