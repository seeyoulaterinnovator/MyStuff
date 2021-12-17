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
        <span class="close"><svg class="eye" width="32" height="32" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path fill-rule="evenodd" clip-rule="evenodd" d="M3.84277 11.4619C9.44922 20.1826 22.5537 20.1816 28.1602 11.4609L29.8428 12.543C28.8818 14.0391 27.7275 15.3096 26.4395 16.3555L28.834 19.9473L27.1699 21.0566L24.8096 17.5166C24.002 18.0195 23.1553 18.4453 22.2832 18.7959L23.459 22.7148L21.543 23.2891L20.3848 19.4287C19.2773 19.7246 18.1416 19.9062 17.001 19.9727V24.002H15.001V19.9727C13.8604 19.9053 12.7246 19.7236 11.6172 19.4277L10.459 23.2891L8.54297 22.7148L9.71875 18.7959C9.26953 18.6152 8.82715 18.4141 8.39355 18.1934C7.98535 17.9863 7.58496 17.7607 7.19336 17.5166L4.83301 21.0566L3.16895 19.9473L5.56348 16.3555C4.27539 15.3096 3.12207 14.0381 2.16016 12.543L3.84277 11.4619Z" fill="black"/>
          </svg></span>
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

