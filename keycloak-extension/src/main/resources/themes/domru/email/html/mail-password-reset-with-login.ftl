<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
    <#elseif section = "body">
        <#--${kcSanitize(msg(emailResetPasswordBodyHtml,authHref, expTimePassAndLogin))?no_esc}
        <#if phone??>
            ${kcSanitize(msg(emailLoginAndPhoneHtml, userName, phone))?no_esc}
        <#else>
            ${kcSanitize(msg(emailLoginHtml, userName))?no_esc}
        </#if>
        ${kcSanitize(msg("login"))?no_esc}
        ${kcSanitize(msg(emailPasswordFooterHtml, phone))?no_esc}-->
        <div>
            <span style="font-size: 18px;   line-height: 24px;">
                Пароль вашей учетной записи был сброшен.<br/>
                Установите новый пароль по <a style="display: inline-block !important" href="${authHref}">ссылке</a>, чтобы завершить процесс <br/> и восстановить доступ к личному кабинету.<br/>
	        </span>

            <div class="content__secondary content__secondary-mt"> Срок действия ссылки ${expTimePassAndLogin}.
            </div>

            <#if email??>
                <div class="content__secondary content__secondary-mt">Ваш логин:</div>
                <div style="font-size: 18px;   font-weight: 700">${email}</div>
            </#if>
            <#if phone ??>
                <div class="content__secondary">или</div>
                <div style="font-size: 18px;   font-weight: 700">${phone}</div>
            </#if>

            <div style="font-size: 18px;  margin-top: 16px"><a href="https://newlkb2b.dom.ru">Перейти в
                    Личный кабинет</a></div>

            <div style="font-size: 18px; line-height: 24px;  font-weight: 400; margin-top: 16px">
                Мы не храним ваши пароли.<br/>
                Если вы забыли свой пароль или у вас не получается войти
                в Личный кабинет — воспользуйтесь формой восстановления пароля по ссылке <a class="no_block" href="https://newlkb2b.dom.ru">«Забыли пароль?»</a>.
            </div>


            <div style="font-size: 12px;  margin-top: 16px;  font-weight: 400">
                <div style="margin-left: 25px">
                    <div class="red-item">

                    </div>
                    Для восстановления данных укажите ваш логин.
                </div>
                <div style="margin-top:8px; margin-left: 25px">
                    <div class="red-item">

                    </div>
                    На ваш адрес электронной почты будет отправлена ссылка для восстановления пароля.<br/>
                    <span style="margin-left: 19px">Срок действия ссылки ${expTimePass}.</span>
                </div>
            </div>
        </div>
    </#if>
</@template.layout>
