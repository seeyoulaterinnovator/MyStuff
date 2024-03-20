<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
        <#include 'styles/content-style--default.html' >
    <#elseif section = "body">
        <#assign email=realmName>
        <#if user?? && user.getEmail??>
            <#assign email= user.getEmail()>
        </#if>
        <p>Была создана учетная запись <a class="no_block" href="mailto:{2}">${email}</a>.
            Если это были вы, пройдите по ссылке для подтверждения вашего email.</p>
        <p style=" margin-top: 16px; margin-bottom: 16px;" class = "block_link" ><a href="${link}">Подтвердить E-mail</a></p>
        <p style="font-size: 12px; line-height: 16px;" class = "small_text">Эта ссылка устареет через ${expTime! "37 часов"}.</p>
        <p style="color: #8c8c8c; font-size: 12px !important; line-height: 16px !important; letter-spacing: 0.02em !important;" class="content__additional small_text">Если Вы не создавали учетную запись, просто проигнорируйте это письмо.</p>
    </#if>
</@template.layout>
