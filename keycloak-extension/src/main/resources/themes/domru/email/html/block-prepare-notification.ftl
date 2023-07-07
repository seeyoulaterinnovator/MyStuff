<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
    <#elseif section = "body">
        ${blockPrepareNotificationSchedulerHtml?no_esc}
        <#if phone??>
            <p class="small_text login_data">Ваш логин:<br><span class="text_bolid">${userName}</span><br><span
                        class="small_text">или</span><br><span class="text_bolid">${phone}</span></p>

        <#else>
            <p class="small_text login_data">Ваш логин:<br><span class="text_bolid">${userName}</span></p>
        </#if>
        <p>Мы не храним ваши пароли.</p>
        <p>Если вы забыли свой пароль или у вас не получается войти в Личный кабинет - воспользуйтесь формой
            восстановления пароля по ссылке <a class="no_block" href="https://newlkb2b.dom.ru">«Забыли пароль?»</a></p>
        <div style="font-size: 12px;  margin-top: 16px;  font-weight: 400">
            <div>
                <span
                        style="background-color: rgba(255, 49, 44, 1); width: 8px; height: 8px; display: inline-block; border-radius: 8px; margin-right: 8px;">

	            </span> Для восстановления данных укажите ваш логин.
            </div>
            <div style="margin-top:8px">
                <span
                        style="background-color: rgba(255, 49, 44, 1); width: 8px; height: 8px; display: inline-block; border-radius: 8px; margin-right: 8px;">

	            </span> На ваш адрес электронной почты будет отправлена ссылка для восстановления пароля. Срок действия ссылки ${expTimePass?no_esc}.
            </div>
        </div>

    </#if>
</@template.layout>