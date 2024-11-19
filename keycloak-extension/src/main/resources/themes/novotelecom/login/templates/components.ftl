<#import "./svg.ftl" as svg>

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
        <div class="passw_ok hidden">
          <@svg.doneIcon/>
        </div>
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
        <div class="field__open nested-svg highlighted-nested-hover-svg" target="${fieldName}">
          <span class="close ">
            <@svg.closedEyeIcon/>
          </span>
          <span class="open hidden">
            <@svg.openedEyeIcon/>
          </span>
        </div>
    </#if>
    </div>
    <#if withError>
        <div class="field__error-message" id="${fieldName}-error-message"></div>
    </#if>
    </div>
</#macro>

