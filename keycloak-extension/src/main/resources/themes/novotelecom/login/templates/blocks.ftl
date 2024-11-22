<#import "./components.ftl" as components>
<#import "./svg.ftl" as svg>

<#macro password firstFieldName="password">
    <div id="password-block">
        <div class="password-instruction"> 
          <p class="text-black">Пароль должен состоять из комбинации букв, цифр и быть не менее 8 и не более 16
              символов</p>
          <div class="flex custom-mt-sm">
              <div id="letters-password" class="flex flex-1 flex-col mr-6">
                  <span class="text-symbols">A-z</span>
                  <span class="sm:block">Латинские символы с верхним и нижним регистром</span>
              </div>
              <div id="numbers-password" class="flex flex-1 flex-col mr-4">
                  <span class="text-symbols">0–9</span>
                  <span class="sm:block">Цифра или несколько цифр</span>
              </div>
          </div>
        </div>
        
        <div class="password-wrapper custom-mt-xl">
          <p class="text-main-800">Не забудьте сохранить или запомнить пароль<span id="generated-password"                                                                     class="text-base"></span></p>
          <div class="flex custom-mt-lg">
              <div class="pass-fields">
                  <@components.field fieldName="${firstFieldName}" label="${msg('password')}" placeholder="${msg(passwordPlaceholder)}" type="password" required=true value="" />
              </div>
              <div class="ml-auto generated-password btn-group flex custom-ml-md">
                  <button id="refresh-password-button" class="focus:outline-none flex-child allowDoubleClick highlighted-nested-svg highlighted-nested-hover-svg"
                          type="button">
                      <@svg.reloadIcon/>
                  </button>
                  <button id="generate-password-button" type="button"
                          class="generate-btn focus:outline-none pass--hidden-small flex-child allowDoubleClick ml-2">
                      <span class="reference reference-generate-password uppercase highlighted-text">Генерировать</span>
                  </button>
              </div>

          </div>
        </div>
      
    </div>
</#macro>

<#macro contentHeader mainTitle secondaryTitle="" secondaryHref="" withBorder=false >
    <div class="page-title-wrapper flex items-center enter-reg-mobile">
        <h1 id="page-title"
            class="<#if secondaryTitle != ''>enter-text</#if>">
            <b class="titleAllPage">
                ${mainTitle}
            </b>
        </h1>

        <#if secondaryTitle != "">
            <h2 class="page-title text-secondary-title highlighted-hover-text">
                <a href="${secondaryHref}">
                    <b class="titleAllPage">
                        ${secondaryTitle}
                    </b>
                </a>
            </h2>
        </#if>
    </div>
</#macro>

<#macro verificationHeader mainTitle>
    <div class="page-title-wrapper">
        <h1 id="page-title" class="verification__title">
            <b class="titleAllPage">${mainTitle}</b>
        </h1>
    </div>
</#macro>

<#macro personalDataProcessAccept>
    <a class="personal-data-process-accept flex flex-col justify-center items-left flex-basis-auto text-xs" href="https://dom.ru/policy.pdf" target="_blink">
      <span class="text-main-1000">
          Нажимая кнопку, вы соглашаетесь <br>
      </span>
      <span class="reference reference_hoverable allowDoubleClick item_hover highlighted-text underline-hovering-text"
        style="font-weight: 350;">
        <#if isLoginFullTexts!false>
            с Условиями обработки персональных данных
        <#else>
            с Условиями обработки данных
        </#if>
      </span>
    </a>
</#macro>
