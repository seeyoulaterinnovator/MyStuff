<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
        <title>Уведомление перед блокировкой</title>
    <#elseif section = "body">
        <p class="content__p">Ваша учетная запись для входа в Личный кабинет «ЭР-Телеком Бизнес» <font color="red" class="text_red">будет заблокирована через ${absence}</font>. Доступ к услугам при этом не блокируется.</p>
        <p class="content__p">Вы не использовали свою учетную запись в течении длительного времени. Для предотвращения блокировки учетной записи, просим Вас войти в <a class="content__a no_block" href="${link}">Личный кабинет</a> до истечения указанного срока.</p>
        <dl class="content__dl">
            <#if phone??>
                <dt class="content__dl-dt">Ваш логин:</dt>
                <dd class="content__dl-dd"><b>${userName}</b></dd>
                <dt class="content__dl-dt">или</dt>
                <dd class="content__dl-dd"><b>${phone}</b></dd>
            <#else>
                <dt class="content__dl-dt">Ваш логин:</dt>
                <dd class="content__dl-dd"><b>${userName}</b></dd>
            </#if>
        </dl>
        <p class="content__p">Мы не храним ваши пароли.</p>
        <p class="content__p">Если вы забыли свой пароль или у вас не получается войти в Личный кабинет - воспользуйтесь формой восстановления пароля по ссылке <a class="content__a no_block" href="http://newlkb2b.dom.ru">«Забыли пароль?»</a></p>
        <ul class="content__ul">
            <li class="content__ul-li">Для восстановления данных укажите ваш логин</li>
            <li class="content__ul-li">На ваш адрес электронной почты будет отправлена ссылка для восстановления пароля.<br>
                Срок действия ссылки 5 минут</li>
        </ul>
    </#if>
</@template.layout>
