<#import "template.ftl" as layout>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayInfo=true displayCity=false displayWarningMessage=false; section>
    <p>BLANK PAGE</p>
    <#if section = "header">
    <#elseif section = "form">
        <form action="${url.loginAction}" method="post">
            <button id="closeWindow" class="hide"
                    type="submit"></button>
        </form>
    </#if>
    <script>
        var actionIsEmpty = ${actionIsEmpty?c};
        var clientIsB2B = ${clientIsB2B?c};

        if (clientIsB2B === true && actionIsEmpty === true) {
        // if (actionIsEmpty === true) {
            window.onunload = function () {
                window.parent.postMessage('post-selected', '*');
                console.log("bp���������� �� B2B � Action ����");
            };
        }
    </script>
</@layout.registrationLayout>
