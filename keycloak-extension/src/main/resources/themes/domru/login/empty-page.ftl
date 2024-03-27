<#import "template.ftl" as layout>
<#import "templates/blocks.ftl" as blocks>

<@layout.registrationLayout displayInfo=true displayCity=false displayWarningMessage=false; section>
    <#if section = "header">
<#--         если section равно "form", то создается форма с кнопкой отправки,
которая автоматически отправляется при загрузке страницы благодаря JavaScript-функции autoSubmit()-->
    <#elseif section = "form">
        <body onload="autoSubmit()">

        <p> EMPTY PAGE </p>

        <form id="form" action="${url.loginAction}" method="post">
            <button  class="hide"
                    type="submit"></button>
        </form>
        </body>
    </#if>
    <script>
        var actionIsEmpty = ${actionIsEmpty?c};
        var clientIsB2B = ${clientIsB2B?c};

        if (actionIsEmpty === true) {
            // if (clientIsB2B === true && actionIsEmpty === true) {
            window.onunload = function () {
                window.parent.postMessage('post-selected', '*');
                console.log("ep���������� �� B2B � Action ����");
            };
        }

        function autoSubmit() {
            console.log("���������� autoSubmit ����");
            document.getElementById("form").submit();
        }
    </script>
</@layout.registrationLayout>