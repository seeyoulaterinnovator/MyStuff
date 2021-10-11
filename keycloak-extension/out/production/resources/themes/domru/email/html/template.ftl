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
                    <a href="https://b2b.dom.ru" target="_blank" style="height: 0;">
                      <img class="logo"
                           alt="ДОМ.РУ Бизнес"
                           height="48"
                           src="data:image/jpeg;base64,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"

                      />
                    </a>
                  </td>
                  <td style="text-align: right;">
                    <a href="tel:88003339000" class="contacts">
                      <table margin="0" padding="0" cellpadding="0" cellspacing="0" style="vertical-align: center; text-align: right; display: inline-table;">
                        <tr>
                          <td style="vertical-align: center;">
                            <img class="contacts__img"
                                 height="48"
                                 src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAACXBIWXMAABYlAAAWJQFJUiTwAAAFL0lEQVRo3u1ZCVPySBBNEA+OCIJCUD9R7htybu3//2M7bL0un12TEN18Yn21U9WlQGbm9fW6Z+I4/48/Y9wYaRt5NjI3sjcSG0khgZG1kamRvpGmkeq5QbtGugAlYLf4PDTyZOQR8mJkZGRhJDTyt5ENvr85lwJVWHVnxDdSL2DVo9LX8NbYSGKkd04vVACKxyWsegwTD9IA8Ipl/o8YDcT2MUxWlhyI4KkZwqZzTvBs8TpCgQEnSmKIfE6RB2sk/VmS+QrWDgEuArgIyqzARjP8XSJpD0qxBJ7pfkdYDSF32DSBAiFo8g1AGhYQLpT2wEpzmhvBK0OE1gLPlQ5eYlmEAXS+wGBzeCEkT4bY5wASKFWByLJZhP/Hn8yfNs0PLf/vy/KCbHhHGyQAfIeNRImifH6BBJYceIVCAa01LzthGegbKTag5D26vVbQmzJnC4UcUkKM9PhfKiyPESVsgI2YMRZElTMAuoQyTYgo1iFPHp+/VXtNidUClQeVIpR7D7oTFrlViSY06ataEFByz0GjIdWFA8DtiHJfaY0mlI/IUCm8LaOPdXPHFAAlRGaWhBUAU7LsPT2nC1dMIr+vaY9HAI4tDBeSF3pY7zavJY6hqQNwO3y3A8gJbSSAerAmAzygeM0QYhsFcI9wGlN4yrwuwlaMMKTQ3inPfRhHVvkLIcEJmtIkF4APBChVIEawmquIoE8Gicg7Ygz2aJ2U3VJIT+A963iBpaqUvFK87tWz9YxC5Bc4+AiFBsRefcuzK1JQaoIPI1zbFl9CQ2mJV9Sr1DJa6Tm5epLR8OnvPJX09zkGFbrtE6kc57ZsE7YUKjVqvFbE1Zpul2RNLwe8Y6HmWIHTo0thJnlwjc8PtgkJjoFCazJ5kbFBndhlWRC4Q/UgoZyxjVuEcEzerWDewGaslKqfR5NnGRs0CzyTdwD6yvquwpmpQJN4fJ4DIj7xjHPCukkBBfT6mQocmeQXhYf0JIscRpGT2AZUWXQMLPFto/VEdbuXqlZ9UGBDpfsKn5MT4GaUjA8Fk/gChCEs1Mp4zicWelaGtZ4/3igZhSITRVuustCOgOwK3vE8q/ZkBMtmMRUD7ijG+zD6+FGKxC+KwYGy4AsB52q6zjmIVLCmbd5GeaIKGpe+6JpqwzarK22glWiThaXCrjDJA8iEcmSpzgsRYrZDd0MDKoyiwEpdCkQAWEUNsB1uVjk5+e/ELfEyn5wi9CqhstyQik6qTlUJ3QslqtlbYl6Lci2kwrmldvyBWCk4dfKT+KxQWMW0eETdaSfjIBIpJWLLbwdqi6uwfKy8qPPqCQpd5ilQU6W9AmvEdNx7VYv01bGyCyuN4f4J3caNSKGtasra8EZEB5qBqgsPXzlidsnF+gqlpsLmVDcqF8LSTo8stBxRKJV2ycUuDsn9MwqTyYka4FLc87lYSGOsbvfqZd5MdNS5OFCns6I3EmyQlA4sC6o50vuUZn25eUjVpRYXo8/e8bt0mxGqxBf26ZTpAQ9W3lOFjij+X+EBt6BBetROBFhvD8vLPWnpl7xNqrJPZKlQFadnnLJaKI4e4txH2O0VZUoxrAH0t7078MD/iaoRiSpiupjxtcsa3jjry44Wepy9uj/KKmQBwrB3qih997hAmPmI4SmATtHpPiK0rn4K4AqqcO+Lr4mayKXGOS2+QnzvYGkfuXHjvL/BdKFgDVX9BfOCT17L/7Yc8BAiG5UHnMhc8LagS9/5AW/rbc1gG5YeOO9v6vvO+/uzHwf6jx2ukt8y/gHy55BqYqsv9QAAAABJRU5ErkJggg=="
                                 alt="Телефон" />
                          </td>
                          <td style="width: 5px;"> </td>
                          <td class="contact__number">
                            8&nbsp;800&nbsp;333&nbsp;9000
                          </td>
                        </tr>
                      </table>
                    </a>
                  </td>
                </tr>
              </table>


              <div class="mail-container__content content">
                <div class="content">
                  <h2 class="content__heading">Уважаемый Клиент!</h2>
                  <#nested "body">
                </div>
              </div>
              <div class="mail-container__footer">
                <p class="mail-text--bold">Благодарим Вас!</p>
                <p>За выбор услуг «Дом.ru Бизнес» для Вашей компании.</p>
                <p class="mail-text--copyright mail-text--light-gray">© АО «ЭР-Телеком Холдинг» 2011-2020</p>
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
