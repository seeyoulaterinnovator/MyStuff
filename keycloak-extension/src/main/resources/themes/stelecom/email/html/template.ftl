<#macro layout >
  <!DOCTYPE html>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="ru">
  <head>
      <#include 'styles/template-style.html' >

      <#nested "style">
  </head>
  <body>
  <table class="mail-wrapper">
    <tr>
      <td>
        <table class="mail-container">
          <tr>
            <td>
              <table margin="0" padding="0" cellpadding="0" cellspacing="0" class="mail-container__header" width="100%">
                <tr>
                  <td style="text-align: left;">
                    <a href="https://lkb2b.stelecom.ru" target="_blank" class="logo-text-wrapper">
                      <p>С-Телеком <span>Бизнес</span></p>
                    </a>
                  </td>
                  <td style="text-align: right;">
                    <a href="tel:84951459555" class="contacts contact__number">
                      +7(495)145 9555
                    </a><br>
                    <a href="https://lkb2b.stelecom.ru" class="contacts contact__link">
                      lkb2b.stelecom.ru
                    </a>
                  </td>
                </tr>
              </table>
              <hr />
              <div class="mail-container__content content">
                <div class="content">
                  <h2 class="content__heading">Уважаемый Клиент!</h2>
                    <#nested "body">
                </div>
              </div>
              <div class="mail-container__footer">
                <p class="mail-text--copyright mail-text--bold">С уважением, С-Телеком!</p>
                <p class="mail-text--copyright mail-text--light-gray">Данное письмо отправлено с адреса, предназначенного только для рассылок. Пожалуйста, не отвечайте на это письмо. В случае возникновения вопросов Вы можете обратиться к персональному менеджеру, контактные данные есть в «личном кабинете» или по телефону +7(495)145 9555 (звонок бесплатный).</p>
                <p class="mail-text--copyright mail-text--light-gray">© OOO «С-Телеком» 2021-${.now?string.yyyy}</p>
              </div>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
  </body>
  </html>
</#macro>
