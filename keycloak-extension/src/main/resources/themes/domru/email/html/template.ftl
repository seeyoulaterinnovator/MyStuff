<#include "./image.ftl" />

<#macro layout >
    <!DOCTYPE html>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="ru">
    <head>
        <#include 'styles/template-style.html' >
        <#nested "style">
    </head>
    <body>
    <table class="mail-wrapper" style="background-color: #ffffff;margin:0;text-align:center;width:100%;">
        <tr>
            <td>
                <table class="mail-container"
                       style="box-sizing: border-box;display:inline-table;font-family:'arial', sans-serif;margin:0 auto 0 auto;max-width:600px;padding:40px 24px 40px 24px;text-align:left;">
                    <tr>
                        <td>
                            <table margin="0" padding="0" cellpadding="0" cellspacing="0" class="mail-container__header"
                                   width="100%">
                                <tr>
                                    <td style="text-align: left; vertical-align: top">
                                        <a href="https://newlkb2b.dom.ru" target="_blank"
                                           style="height:0;margin:0; text-decoration: none;">
                                            <table>
                                                <tr>
                                                    <td>
                                                        <img class="logo"
                                                             alt="ДОМ.РУ Бизнес"
                                                             height="24"
                                                             width="230"
                                                             src="${logoPngBase64}"
                                                        />
                                                    </td>
                                                </tr>
                                            </table>
                                        </a>
                                    </td>
                                    <td style="text-align: right; vertical-align: top">
                                        <table margin="0" padding="0" cellpadding="0" cellspacing="0"
                                               style="vertical-align: center; text-align: right; display: inline-table;">
                                            <tr>
                                                <td class="contact__number">
                                                    <a href="${phoneConstLink}" class="contacts">
                                                        8 800 250 0333
                                                    </a>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td align="right" class="system">
                                                    <a href="https://newlkb2b.dom.ru" class="system_link">lkb2b.dom.ru</a>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>

                            <div class="mail-container__content content">
                                <div class="content">
                                    <h2 style="margin-top: 30px; font-size: 32px" class="content__heading">Уважаемый клиент!</h2>
                                    <#nested "body">
                                </div>
                            </div>

                            <div>
                                <p class="mail-text-title" style="font-weight: 700; font-size: 24px;">Всегда на связи!</p>
                                <table height="172" class="messengers" cellpadding="0" cellspacing="0" width="100%">
                                    <tr>
                                        <td style="width: 25%">
                                            <img style="width: 120px; height: 120px"
                                                 src="${telegramQr}">
                                        </td>
                                        <td style="width: 25%">
                                            <img style="width: 120px; height: 120px"
                                                 src="${whatsAppQr}">
                                        </td>
                                        <td style="width: 25%">
                                            <img style="width: 120px; height: 120px"
                                                 src="${b2bDomruRuQr}">
                                        </td>
                                        <td style="width: 25%">
                                            <img style="width: 120px; height: 120px"
                                                 src="${mobileAppQr}">
                                        </td>
                                    </tr>
                                    <tr class="messengers-links">
                                        <td style="width: 25%">
                                            <a href="https://t.me/DomruBusinessBot">Telegram</a>
                                        </td>
                                        <td style="width: 25%">
                                            <a href="https://wa.me/73422195440">WhatsApp</a>
                                        </td>
                                        <td style="width: 25%">
                                            <a href="https://b2b.domru.ru/?openchat">b2b.domru.ru</a>
                                        </td>
                                        <td style="width: 25%">
                                            <a href="https://app.b2b.dom.ru/mobile-a">Мобильное приложение</a>
                                        </td>
                                    </tr>
                                </table>
                                <p class="mail-text-title mail-text--bold" style="font-weight:bold;margin:40px 0 16px 0">${gratitudeUp}</p>
                                <p class="mail-text--translucent mail-text--light-gray" style="font-size:12px;line-height:16px;margin:8px 0 0 0">${gratitudeDown?no_esc}</p>
                                <p class="mail-text--translucent mail-text--light-gray" style="font-size:12px;line-height:16px;margin:8px 0 0 0">${footerInMassage}${.now?string.yyyy}</p>
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
