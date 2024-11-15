<#include "./helpers/images.ftl" />
<#include "./helpers/variables.ftl" />
<#import "./helpers/blocks.ftl" as blocks>

<#macro layout>
  <!DOCTYPE html>
  <html 
    xmlns="http://www.w3.org/1999/xhtml" 
    xml:lang="ru"
    lang="ru"
    style="background:#ffffff!important; color:#000000!important;"
  >
    <head>
      <meta content="text/html; charset=utf-8" http-equiv="Content-Type" />
      <meta content="width=device-width" name="viewport" />
      <#include 'styles/template-style.html'> 
      <#nested "style">
    </head>
    <body>
      <table
        class="mail-wrapper"
        style="
          background-color: #ffffff;
          margin: 0;
          text-align: center;
          width: 100%;
        "
      >
        <tr>
          <td>
            <table
              class="mail-container"
              style="
                box-sizing: border-box;
                display: inline-table;
                font-family: 'arial', sans-serif;
                margin: 0 auto 0 auto;
                max-width: 600px;
                padding: 40px 24px 40px 24px;
                text-align: left;
              "
            >
              <tr>
                <td>
                  <table
                    margin="0"
                    padding="0"
                    cellpadding="0"
                    cellspacing="0"
                    class="mail-container__header"
                    width="100%"
                  >
                    <tr>
                      <td style="text-align: left; vertical-align: top">
                        <a
                          href="${homePageSystemLinkInMessage}"
                          target="_blank"
                          style="height: 0; margin: 0; text-decoration: none"
                        >
                          <table>
                            <tr>
                              <td>
                                <img
                                  class="logo"
                                  alt="${systemName}"
                                  height="25.6"
                                  width="157"
                                  src="${logoImg}"
                                />
                              </td>
                            </tr>
                          </table>
                        </a>
                      </td>
                      <td style="text-align: right; vertical-align: top">
                        <table
                          margin="0"
                          padding="0"
                          cellpadding="0"
                          cellspacing="0"
                          style="
                            vertical-align: center;
                            text-align: right;
                            display: inline-table;
                          "
                        >
                          <tr>
                            <td class="contact__number">
                              <a href="tel:${phoneLinkInMessage}" class="contacts">
                                ${phoneTextInMessage}
                              </a>
                            </td>
                          </tr>
                          <tr>
                            <td align="right" class="system">
                              <a
                                href="${homePageSystemLinkInMessage}"
                                class="system_link"
                              >
                                ${homePageSystemTextInMessage}
                              </a>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>

                  <div class="mail-container__content content">
                    <div class="content">
                      <h2
                        style="
                          margin-top: 40px;
                          margin-bottom: 16px;
                          font-size: 32px;
                        "
                        class="content__heading"
                      >
                        ${customer!}
                      </h2>
                      <#nested "body">
                    </div>
                  </div>

                  <div>
                    <p
                      class="mail-text-title-secondary mail-text--bold"
                      style="font-weight: bold; margin: 40px 0 16px 0"
                    >
                      <@blocks.parameterizedMsg message=gratitudeUp />
                    </p>
                    <p
                      class="mail-text--translucent mail-text--light-gray"
                      style="
                        font-size: 12px;
                        line-height: 16px;
                        margin: 8px 0 0 0;
                      "
                    >
                      <@blocks.parameterizedMsg message=gratitudeDown/>
                    </p>
                    <p
                      class="mail-text--translucent mail-text--light-gray"
                      style="
                        font-size: 12px;
                        line-height: 16px;
                        margin: 8px 0 0 0;
                      "
                    >
                      ${footerInMassage}${.now?string.yyyy}
                    </p>
                  </div>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
      <!-- prevent Gmail on iOS font size manipulation
        <div
          style="display: none; white-space: nowrap; font: 15px courier; line-height: 0"
        >
          &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
          &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
          &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
        </div>
      -->    
    </body>
  </html>
</#macro>
