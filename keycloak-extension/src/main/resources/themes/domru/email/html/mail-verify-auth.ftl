<#import "template.ftl" as template>

<@template.layout ; section>
  <#if section = "style">
    <#include 'styles/content-style--default.html' >
  <#elseif section = "body">
    <p>Код для подтверждения данных вашей учетной записи Личного кабинета «Дом.ру Бизнес»:</p><p style="font-weight: bold" class = "text_bolid margin_block">${code}</p>
  </#if>
</@template.layout>
