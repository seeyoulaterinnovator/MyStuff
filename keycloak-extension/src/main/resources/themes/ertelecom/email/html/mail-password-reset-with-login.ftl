<#import "template.ftl" as template>
<#import "./helpers/blocks.ftl" as blocks>

<#--  Admin console - Users – Select user – Send login and reset password -->
<@template.layout ; section>
  <#if section="style">
    <title>${kcSanitize(msg(emailResetPasswordSubject!""))}</title>
  <#elseif section="body">
    <@blocks.parameterizedMsg message=emailResetPasswordBodyHtml/>
  </#if>
</@template.layout>

<#--  TODO: Old logic  -->
<#--  <#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
        <title>${kcSanitize(msg("emailResetPasswordSubject"))}</title>
    <#elseif section = "body">
        ${kcSanitize(msg("emailResetPasswordBodyHtml",authHref))?no_esc}
        <#if phone??>
            ${kcSanitize(msg("emailLoginAndPhoneHtml", userName, phone))?no_esc}
        <#else>
            ${kcSanitize(msg("emailLoginHtml", userName))?no_esc}
        </#if>
        ${kcSanitize(msg("emailPasswordFooterHtml"))?no_esc}
    </#if>
</@template.layout>  -->

<#--    <#if section = "style">-->
<#--        <#include 'styles/custom-content-style.html' >-->
<#--    <#elseif section = "body">-->
<#--        <div style=" color: #222; font-family: Arial;">-->
<#--            <span style="font-size: 18px;   line-height: 24px;">-->
<#--                Пароль вашей учетной записи был сброшен!<br/>-->
<#--                Установите новый пароль по <a href="${authHref}">ссылке</a>, чтобы завершить <br/> процесс и восстановить доступ к личному кабинету.<br/>-->
<#--	        </span>-->

<#--            <div style="font-size: 12px;   font-weight: 400; margin-top: 16px">-->
<#--                Срок действия ссылки ${expTimePassAndLogin}.-->
<#--            </div>-->

<#--            <#if email??>-->
<#--                <div style="font-size: 12px;   font-weight: 400; margin-top: 16px">Ваш логин:</div>-->
<#--                <div style="font-size: 18px;   font-weight: 700">${email}</div>-->
<#--            </#if>-->
<#--            <#if phone ??>-->
<#--                <div style="font-size: 12px;   font-weight: 400">или</div>-->
<#--                <div style="font-size: 18px;   font-weight: 700">${phone}</div>-->
<#--            </#if>-->

<#--            <div style="font-size: 18px;  margin-top: 16px"><a href="https://lk.ertelecom.ru">Перейти в-->
<#--                    Личный кабинет</a></div>-->

<#--            <div style="font-size: 18px; line-height: 24px;  font-weight: 400; margin-top: 16px">-->
<#--                Мы не храним ваши пароли.<br/>-->
<#--                Если вы забыли свой пароль или у вас не получается войти-->
<#--                в Личный кабинет — воспользуйтесь формой восстановления пароля по ссылке <a class="no_block" href="https://lk.ertelecom.ru">«Забыли пароль?»</a>.-->
<#--            </div>-->


<#--            <div style="font-size: 12px;  margin-top: 16px;  font-weight: 400">-->
<#--                <div style="margin-left: 25px">-->
<#--                    <div class="red-item">-->

<#--                    </div>-->
<#--                    Для восстановления данных укажите ваш логин.-->
<#--                </div>-->
<#--                <div style="margin-top:8px; margin-left: 25px">-->
<#--                    <div class="red-item">-->

<#--                    </div>-->
<#--                    На ваш адрес электронной почты будет отправлена ссылка для восстановления пароля.<br/>-->
<#--                    <span style="margin-left: 19px">Срок действия ссылки ${expTimePass}.</span>-->
<#--                </div>-->
<#--            </div>-->
<#--        </div>-->
<#--    </#if>-->
