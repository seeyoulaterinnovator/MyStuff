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
        <p>
        <ul class="instruction content__ul">
            <li class="small_text instruction_text content__ul-li">Для восстановления данных
                укажите ваш логин
            </li>
            <li class="small_text content__ul-li">На ваш адрес электронной почты будет отправлена
                ссылка для восстановления пароля.<br>Срок действия ссылки ${expTimePass?no_esc}
            </li>
        </ul>
        </p>
    </#if>
</@template.layout>