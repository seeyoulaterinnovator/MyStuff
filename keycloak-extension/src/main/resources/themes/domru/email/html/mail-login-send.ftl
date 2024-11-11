<#import "template.ftl" as template>
<#import "blocks.ftl" as blocks>

<@template.layout ; section>

    <#if section = "style">
    <#elseif section = "body">
        <p style="font-size: 18px">Для авторизации в <a class="no_block" href="https://newlkb2b.dom.ru">Личном кабинете</a> необходимо указать логин и пароль, установленный вами при регистрации в Личном кабинете.</p>
        <@blocks.yourLogin login="${userName!}" phone="${phone!}" />
        <@blocks.recoveryPasswordInstruction/>
        ${kcSanitize(msg("login"))?no_esc}
    </#if>
</@template.layout>
