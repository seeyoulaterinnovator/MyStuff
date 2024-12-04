<#import "template.ftl" as layout>
  <@layout.registrationLayout; section>
    <#if section="header">
      <div class="page-title-wrapper">
        <h1 id="page-title">
          <b class="titleAllPage">
            ${msg("pageExpiredTitle")}
          </b>
        </h1>
      </div>
      <#elseif section="form">
        <p id="instruction1" class="instruction">
          ${msg(pageExpiredMsg1)}
          <a id="loginRestartLink" href="${url.loginRestartFlowUrl}" class="highlighted-text underline-hovering-text">
            ${msg(doClickHere)}
          </a><br />
          ${msg(pageExpiredMsg2)}
          <a id="loginContinueLink" href="${url.loginAction}" class="highlighted-text underline-hovering-text">
            ${msg(doClickHere)}
          </a>
        </p>
    </#if>
    </@layout.registrationLayout>