<#macro field fieldName label="" placeholder="" required=false withError=true type="text" class="" extra... >
  <div 
    class="field <#if required>field--required</#if> ${class}"
    <#list extra as attrName, attrVal>
      ${attrName}="${attrVal}"
    </#list>
  >
    <#if label??>
      <label 
        class="field__label" 
        for="${fieldName}">
          ${label}
      </label>
    </#if>
    <input 
      class="field__input" 
      name="${fieldName}" 
      id="${fieldName}"
      placeholder="${placeholder}"
      type="${type}"
    />
    <#if withError>
      <div class="field__error-message" id="${fieldName}-error-message"></div>
    </#if>
  </div>
</#macro>

