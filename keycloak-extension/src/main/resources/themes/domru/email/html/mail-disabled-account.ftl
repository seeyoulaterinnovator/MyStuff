<#import "template.ftl" as template>
<#import "blocks.ftl" as blocks>

<@template.layout ; section>
  <#if section="style">
    <#elseif section="body">
      <p style="font-size: 18px">Ваша учетная запись для входа в Личный кабинет «Дом.ру Бизнес» <font color="#bd1b1d">заблокирована</font>.
        Доступ к услугам при этом не блокируется.<br>
        Вы не использовали свою учетную запись в течении длительного времени. Чтобы восстановить доступ к учетной записи, обратитесь к своему персональному менеджеру или оставьте заявку в чате
        <a class="no_block" href="https://newlkb2b.dom.ru">на сайте</a> или по телефону
        <a class="no_block" href="tel:88002500333">8 800 2500 333</a>.
      </p>
      <@blocks.yourLogin login="${email!}" phone="${phone!}" />
      <@blocks.recoveryPasswordInstruction/>
  </#if>
</@template.layout>