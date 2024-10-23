<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
    <#elseif section = "body">
        <p style="font-size: 18px">Ваша учетная запись для входа в Личный кабинет «Дом.ру Бизнес» <font color="#bd1b1d">заблокирована</font>.
            Доступ к услугам при этом не блокируется.</p>
        <p>Чтобы восстановить доступ к учетной записи, обратитесь к своему персональному менеджеру или оставьте заявку в чате
            <a class="no_block" href="https://newlkb2b.dom.ru">Личного кабинета</a>, или по телефону
            <a class="no_block" href="tel:88002500333">8 800 2500 333</a>.</p>
        <#--<#if phone??>
            ${kcSanitize(msg(emailLoginAndPhoneHtml, userName, phone))?no_esc}
        <#else>
            ${kcSanitize(msg(emailLoginHtml, userName))?no_esc}
        </#if>
        ${kcSanitize(msg(emailPasswordFooterHtml, expTimePass))?no_esc}-->
    </#if>
</@template.layout>
