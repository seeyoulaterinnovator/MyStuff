<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "header">

    <#elseif section = "form">

        <script>

            var redirectTo = '${redirectTo!}';
            var redirectHeader = '${redirectHeader!}';

            window.location = redirectTo;

        </script>

    </#if>
</@layout.registrationLayout>