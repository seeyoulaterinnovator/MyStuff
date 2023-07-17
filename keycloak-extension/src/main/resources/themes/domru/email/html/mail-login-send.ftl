<#import "template.ftl" as template>

<@template.layout ; section>

    <#if section = "style">
        <#include 'styles/content-style--default.html' >
    <#elseif section = "body">
        <p style="font-size: 18px">Для авторизации в <a class="no_block" href="https://newlkb2b.dom.ru">Личном кабинете</a> необходимо указать логин и пароль, установленный вами при регистрации в Личном кабинете.</p>
        <#if phone??>
            <p class = "small_text login_data ">Ваш логин:<br><span style="font-weight: bold; font-size: 18px; line-height: 24px;">${userName}</span>
                <br>
                <span class = "small_text">или</span><br><span  style="font-weight: bold; font-size: 18px" class = "text_bolid">${phone}</span>
            </p>
        <#else>
            <p class = "small_text login_data">Ваш логин:<br><span style="font-weight: bold; font-size: 18px; line-height: 24px;">${userName}</span></p>
        </#if>
        <p style="font-size: 18px">Мы не храним ваши пароли.</p>
        <p style="font-size: 18px">Если вы забыли свой пароль или у вас не получается войти в Личный кабинет
            - воспользуйтесь формой восстановления пароля по ссылке
            <a class="no_block" href="https://newlkb2b.dom.ru">«Забыли пароль?»</a></p>

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
                <span style="margin-left: 18px">Срок действия ссылки ${expTimePass}.</span>
            </div>
        </div>
    </#if>
</@template.layout>