<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "header">
<#--        ${msg("termsTitle")}-->
    <#elseIf section = "form">
        <div id="kc-terms-text">
<#--            ${kcSanitize(msg("termsText"))?no_esc}-->
        </div>

        <div class="table-wrapper">
            <h1 class="title">Выбрать организацию</h1>

            <div id="post" class="table scrollable-container overflow-x-hidden overflow-y-auto">
                <div class="trow theader">
                    <div class="org-cell">Организация</div>
                    <div class="role-cell">Роль пользователя</div>
                </div>
                <#list posts as post>
                        <#assign firstRow = post?index == 0>
                        <div class="${firstRow?then('selected', '')} trow titems">
                            <div id="tomsId-${post?index}" class="org-cell">${post.tomsId!}</div>
                            <div id="roleName-${post?index}" class="role-cell">${post.roleName!}</div>
                        </div>
                </#list>
            </div>
        </div>

        <form class="form-actions" action="${url.loginAction}" method="POST">
            <input style="visibility:hidden" type="text" name="roleName" id="roleName" value="${posts[0].roleName!}"/>
            <input style="visibility:hidden" type="text" name="tomsId" id="tomsId" value="${posts[0].tomsId!}"/>
            <input style="visibility:hidden" name="accept" id="kc-accept" type="submit" value="Отправить"/>
        </form>

        <div class="clearfix"></div>
    </#if>

    <script>
        var table = document.getElementById('post');
        Array.from(document.getElementsByClassName('titems')).forEach(function (el, index) {
            var roleName = document.getElementById("roleName-" + index).textContent;
            var tomsId = document.getElementById("tomsId-" + index).textContent;
            el.addEventListener('click', function () {
                document.getElementById('roleName').value = roleName;
                document.getElementById('tomsId').value = tomsId;
                document.getElementById('kc-accept').click();
            })
        });

    </script>
</@layout.registrationLayout>