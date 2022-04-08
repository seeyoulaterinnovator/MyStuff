<#macro field fieldName value="" label="" placeholder="" required=false withError=true type="text" class="" extra... >
  <div
    class="field ${class}
    <#if required>field--required</#if>"
    <#list extra as attrName, attrVal>
      ${attrName}="${attrVal}"
    </#list>
  >

  <div class="field__container">
    <#if type == 'password'>
      <div class="passw_ok hidden"><svg width="14" height="11" viewBox="0 0 14 11" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path fill-rule="evenodd" clip-rule="evenodd" d="M5.16361 7.18734L2.3947 4.41833L0.980469 5.83256L5.22293 10.075L13.0606 2.35629L11.5871 0.882812L5.16361 7.18734Z" fill="#69BE28"/>
        </svg></div>
    </#if>

    <input
        class="field__input" 
        name="${fieldName}" 
        id="${fieldName}"
        placeholder="${placeholder}"
        type="${type}"
        value="${value}"
      />
      <#if label??>
        <label
                class="field__label"
                for="${fieldName}">
            ${label}
        </label>
      </#if>
      <#if type == 'password'>
      <div class="field__open" target="${fieldName}">
        <span class="close">
          <svg width="24" height="8" viewBox="0 0 24 8" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M1 1.00002C6.5 9.24999 17.5 9.25 23 1.00002" stroke="#7585A1"/>
          </svg>
        </span>
        <span class="open hidden"><svg class="eye" width="32" height="32" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path fill-rule="evenodd" clip-rule="evenodd" d="M21.8877 13.21C24.3564 14.2793 26.5625 16.0557 28.1602 18.541L29.8428 17.459C26.6455 12.4863 21.3232 10 16.001 10C10.6787 10 5.35645 12.4863 2.16016 17.459L3.84277 18.54C5.44043 16.0547 7.64746 14.2783 10.1152 13.21C9.41016 14.3027 9.00098 15.6035 9.00098 17C9.00098 20.8662 12.1348 24 16.001 24C19.8672 24 23.001 20.8662 23.001 17C23.001 15.6035 22.5928 14.3027 21.8877 13.21ZM16.001 12C13.2393 12 11.001 14.2383 11.001 17C11.001 19.7617 13.2393 22 16.001 22C18.7627 22 21.001 19.7617 21.001 17C21.001 14.2383 18.7627 12 16.001 12Z" fill="black"/>
          </svg></span>

      </div>
      </#if>
    </div>
    <#if withError>
      <div class="field__error-message" id="${fieldName}-error-message"></div>
    </#if>
  </div>
</#macro>

