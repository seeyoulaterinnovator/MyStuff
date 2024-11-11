<#import "template.ftl" as template>

<@template.layout ; section>
    <#if section = "style">
    <#elseif section = "body">
        <#assign email=realmName>
        <#if user?? && user.getEmail??>
            <#assign email= user.getEmail()>
        </#if>
        <p>Создана учетная запись <a class="no_block" href="mailto:{2}">${email}</a>.
          Если ее создали вы, пройдите по ссылке ниже для подтверждения вашего e-mail.</p>
        <p style=" margin-top: 16px; margin-bottom: 16px;" class = "block_link" ><a href="${link}">Подтвердить e-mail</a></p>
        <p style="font-size: 12px; line-height: 16px;" class = "small_text">
          Эта ссылка устареет через ${expTime! "36 часов"}.<br>
          Если вы не создавали учетную запись, просто проигнорируйте это письмо.
        </p>
    </#if>
</@template.layout>
