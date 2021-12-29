<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section="style">
        <#include 'styles/content-style--default.html' >
    <#elseif section="body">
        <p>Ваша учетная запись для входа в Личный кабинет «С-Телеком Бизнес» <span class="text_red">заблокирована</span>. Доступ к услугам при этом не блокируется.</p>
        <p>Вы не использовали свою учетную запись в течении длительного времени. Чтобы восстановить доступ к учетной записи, обратитесь к своему персональному менеджеру или оставьте заявку в чате <a class="no_block" href="https://lkb2b.stelecom.ru">Личного кабинета</a>, или по телефону <a class="no_block" href="tel:88003339000">8 800 333 9000</a>.</p>
        <dl>
            <#if phone??>
                <dt>Ваш логин:</dt>
                <dd>${userName}</dd>
                <dt>или</dt>
                <dd>${phone}</dd>
            <#else>
                <dt>Ваш логин:</dt>
                <dd>${userName}</dd>
            </#if>
        </dl>
        <p>Мы не храним ваши пароли.</p>
        <p>Если вы забыли свой пароль или у вас не получается войти в Личный кабинет - воспользуйтесь формой восстановления пароля по ссылке <a class="no_block" href="https://lkb2b.stelecom.ru">«Забыли пароль?»</a></p>
        <ul class="instruction">
            <li class="small_text instruction_text"><span class="squares">&#11200;</span>Для восстановления данных укажите ваш логин</li>
            <li class="small_text"><span class="squares">&#11200;</span>На ваш адрес электронной почты будет отправлена ссылка для восстановления пароля.<br>
                Срок действия ссылки 5 минут</li>
        </ul>
    </#if>
</@template.layout>
