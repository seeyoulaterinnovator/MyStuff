<#macro defaultTemplate email="" backHref="/" buttonExist=true isVerified=false success = true>
    <div class="container">
        <header class="page-title-wrapper">
            <h1 id="page-title">
                <b class="titleAllPage"><#nested "header"></b>
            </h1>
        </header>
        <#if !isVerified>
            <p>
                <#nested "description">
            </p>
        </#if>
        <#if buttonExist>
            <div class="page-buttons">
                <#if success>
                    <a class="btn btn-main text-center btn--thanks" href="${url.loginUrl}">Понятно</a>
                <#else>
                    <a class="btn btn-main text-center btn--thanks" href="${url.loginUrl}">На главную</a>
                </#if>
            </div>
        </#if>
    </div>
</#macro>
