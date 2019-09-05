<style>
    .selected { background: #9a9b9c; }
    .attr {
        margin-bottom: 15px;
    }
</style>
<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "header">
        ${msg("termsTitle")}
    <#elseif section = "form">
        <div id="kc-terms-text">
            ${kcSanitize(msg("termsText"))?no_esc}
        </div>
        <div>
            <table id="post" style="width:100%" class="attr">
                <thead>
                <tr>
                    <th>Post ID</th>
                    <th>Post</th>
                </tr>
                </thead>
                <tbody>
                <#list posts as post>
                    <#assign firstRow = post?index == 0>
                    <tr class="${firstRow?then('selected', '')} attr_row" toms_id="${post.tomsId}">
                        <td id="roleId-${post?index}">${post.roleId}</td>
                        <td id="roleName-${post?index}">${post.roleName}</td>
                    </tr>
                </#list>
                </tbody>
            </table>
            <form class="form-actions" action="${url.loginAction}" method="POST">
                <input style="visibility:hidden" type="text" name="roleId" id="roleId" value="${posts[0].roleId}">
                <input style="visibility:hidden" type="text" name="roleName" id="roleName" value="${posts[0].roleName}">
                <input style="visibility:hidden" type="text" name="tomsId" id="tomsId" value="${posts[0].tomsId}">
                <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" name="accept" id="kc-accept" type="submit" value="Отправить"/>
            </form>
        </div>
        <div class="clearfix"></div>
    </#if>
</@layout.registrationLayout>

<script>
    var table = document.getElementById('post');
    Array.from(document.getElementsByClassName('attr_row')).forEach(function (el, index) {
        var roleId = document.getElementById("roleId-" + index).textContent;
        var roleName = document.getElementById("roleName-" + index).textContent;
        var tomsId = el.getAttribute("toms_id");
        el.addEventListener('click', function () {
            document.getElementById('roleId').value = roleId;
            document.getElementById('roleName').value = roleName;
            document.getElementById('tomsId').value = tomsId;
            table.getElementsByClassName('selected')[0].classList.remove('selected');
            this.classList.add('selected');
        })
    });

</script>