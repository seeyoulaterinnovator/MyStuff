<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "header">

    <#elseif section = "form">

        <script>

            const redirectTo = '${redirectTo!}';
            const redirectHeader = '${redirectHeader!}';

            let form = document.createElement('form');
            form.method = 'POST';
            form.action = redirectTo;
            form.style.display = 'none';
            document.body.appendChild(form);

            let el1 = document.createElement('input');
            el1.name = 'btoken';
            el1.value = redirectHeader;
            el1.type = "hidden";
            form.appendChild(el1);

            let el2 = document.createElement('button');
            el2.type = "submit";
            form.appendChild(el2);

            form.submit();

            window.location = redirectTo;

        </script>

    </#if>
</@layout.registrationLayout>