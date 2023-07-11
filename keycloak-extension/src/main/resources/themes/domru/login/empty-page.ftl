<#import "template.ftl" as layout>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayInfo=true displayCity=false displayWarningMessage=false; section>
    <#if section = "header">
    <#elseif section = "form">
        <body onload="autoSubmit()">
        <form id="form" action="${url.loginAction}" method="post">
            <button  class="hide"
                    type="submit"></button>
        </form>
        </body>
    </#if>
    <script>
        var actionIsEmpty = ${actionIsEmpty?c};
        var clientIsB2B = ${clientIsB2B?c};

        if (clientIsB2B === true && actionIsEmpty === true) {
            window.onunload = function () {
                window.parent.postMessage('post-selected', '*');
                console.log("Отправлено тк B2B и Action пуст");
            };
        }

        function autoSubmit() {
            document.getElementById("form").submit();
        }
    </script>
</@layout.registrationLayout>