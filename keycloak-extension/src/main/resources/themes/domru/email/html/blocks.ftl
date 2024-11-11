<#macro yourLogin login="" phone="">
  <#assign hasLogin = login?? && login?has_content>
  <#assign hasPhone = phone?? && phone?has_content>

  <#if hasLogin || hasPhone>
    <div class="content__secondary content__secondary-mt">Ваш логин:</div>
  </#if>
  <#if hasLogin>
    <div style="font-size: 18px; font-weight: 700;">
      ${login}
    </div>
  </#if>
  <#if hasPhone>
    <#if hasLogin>
      <div class="content__secondary">или</div>
    </#if>
    <div style="font-size: 18px; font-weight: 700;">
      ${phone}
    </div>
  </#if>
</#macro>

<#macro recoveryPasswordInstruction>
  <p style="font-size: 18px">
    Мы не храним ваши пароли.<br>
    Если вы забыли свой пароль или у вас не получается войти в Личный кабинет — воспользуйтесь формой
    восстановления пароля по ссылке <a class="no_block" href="https://newlkb2b.dom.ru">«Забыли пароль?»</a>.</p>
  <p>
  <div class="instruction">
    <div class="instruction_text">
      <div class="red-item">&nbsp;</div>
      Для восстановления данных укажите ваш логин.
    </div>
    <div class="instruction_text">
      <div class="red-item">&nbsp;</div>
      На ваш адрес электронной почты будет отправлена
      ссылка для восстановления пароля.<br>
      <span style="margin-left: 19px">Срок действия ссылки ${expTimePass?no_esc}.</span>
    </div>
  </div>
</#macro>