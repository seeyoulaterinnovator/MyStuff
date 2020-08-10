<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "header">
    <#--        ${msg("termsTitle")}-->
    <#elseIf section = "form">
        <div id="kc-terms-text">
            <#--            ${kcSanitize(msg("termsText"))?no_esc}-->
        </div>

        <div class="table-wrapper">
            <#if posts?size gt 1>
            <h1 class="title">Выбрать организацию</h1>
            <#else>
            <h1 class="title" style="visibility: hidden">Выбрать организацию</h1>
            </#if>

            <div id="post" class="table overflow-x-hidden overflow-y-auto">
                <div class="trow theader">
                    <div class="org-cell">Организация</div>
                    <#--                    <div class="org-cell">Уникальный номер</div>-->
                    <div class="role-cell">Роль пользователя</div>
                </div>
                <#list posts as post>
                    <#assign firstRow = post?index == 0>
                    <div class="${firstRow?then('selected', '')} trow titems">
                        <div id="tomsId-${post?index}" style="display:none">${post.tomsId!}</div>
                        <div id="postId-${post?index}" style="display:none">${post.id!}</div>
                        <div id="tomsName-${post?index}" class="org-cell">
                            <#if (post.organization?hasContent && post.organization?length > 1)>
                                ${post.organization}
                            <#else>
                                ${post.tomsId}
                            </#if>
                        </div>
                        <div id="roleName-${post?index}" class="role-cell">
                            <#if (post.userRole.description?hasContent && post.userRole.description?length > 1)>
                                ${post.userRole.description}
                            <#else>
                                ${post.userRole.name}
                            </#if>
                        </div>
                    </div>
                </#list>
            </div>
        </div>

        <form class="form-actions" action="${url.loginAction}" method="POST"> <!-- onsubmit="return selectCustomer(this)"> -->
            <input style="visibility:hidden" type="text" name="roleName" id="roleName" value="${posts[0].roleName!}"/>
            <input style="visibility:hidden" type="text" name="tomsId" id="tomsId" value="${posts[0].tomsId!}"/>
            <input style="visibility:hidden" type="text" name="postId" id="postId" value="${posts[0].id!}"/>
            <input style="visibility:hidden" name="accept" id="kc-accept" type="submit" value="Отправить"/>
        </form>

        <div class="clearfix"></div>
    </#if>

    <script> <#-- fuck IE -___________- -->
        if (!Array.from) {
            Array.from = (function () {
                var toStr = Object.prototype.toString;
                var isCallable = function (fn) {
                    return typeof fn === 'function' || toStr.call(fn) === '[object Function]';
                };
                var toInteger = function (value) {
                    var number = Number(value);
                    if (isNaN(number)) {
                        return 0;
                    }
                    if (number === 0 || !isFinite(number)) {
                        return number;
                    }
                    return (number > 0 ? 1 : -1) * Math.floor(Math.abs(number));
                };
                var maxSafeInteger = Math.pow(2, 53) - 1;
                var toLength = function (value) {
                    var len = toInteger(value);
                    return Math.min(Math.max(len, 0), maxSafeInteger);
                };

                // The length property of the from method is 1.
                return function from(arrayLike/*, mapFn, thisArg */) {
                    // 1. Let C be the this value.
                    var C = this;

                    // 2. Let items be ToObject(arrayLike).
                    var items = Object(arrayLike);

                    // 3. ReturnIfAbrupt(items).
                    if (arrayLike == null) {
                        throw new TypeError('Array.from requires an array-like object - not null or undefined');
                    }

                    // 4. If mapfn is undefined, then let mapping be false.
                    var mapFn = arguments.length > 1 ? arguments[1] : void undefined;
                    var T;
                    if (typeof mapFn !== 'undefined') {
                        // 5. else
                        // 5. a If IsCallable(mapfn) is false, throw a TypeError exception.
                        if (!isCallable(mapFn)) {
                            throw new TypeError('Array.from: when provided, the second argument must be a function');
                        }

                        // 5. b. If thisArg was supplied, let T be thisArg; else let T be undefined.
                        if (arguments.length > 2) {
                            T = arguments[2];
                        }
                    }

                    // 10. Let lenValue be Get(items, "length").
                    // 11. Let len be ToLength(lenValue).
                    var len = toLength(items.length);

                    // 13. If IsConstructor(C) is true, then
                    // 13. a. Let A be the result of calling the [[Construct]] internal method
                    // of C with an argument list containing the single item len.
                    // 14. a. Else, Let A be ArrayCreate(len).
                    var A = isCallable(C) ? Object(new C(len)) : new Array(len);

                    // 16. Let k be 0.
                    var k = 0;
                    // 17. Repeat, while k < len… (also steps a - h)
                    var kValue;
                    while (k < len) {
                        kValue = items[k];
                        if (mapFn) {
                            A[k] = typeof T === 'undefined' ? mapFn(kValue, k) : mapFn.call(T, kValue, k);
                        } else {
                            A[k] = kValue;
                        }
                        k += 1;
                    }
                    // 18. Let putStatus be Put(A, "length", len, true).
                    A.length = len;
                    // 20. Return A.
                    return A;
                };
            }());
        }
    </script>

    <script>
        var table = document.getElementById('post');
        Array.from(document.getElementsByClassName('titems')).forEach(function (el, index) {
            var roleName = document.getElementById("roleName-" + index).textContent;
            var tomsId = document.getElementById("tomsId-" + index).textContent;
            var postId = document.getElementById("postId-" + index).textContent;
            el.addEventListener('click', function () {
                document.getElementById('roleName').value = roleName;
                document.getElementById('tomsId').value = tomsId;
                document.getElementById('postId').value = postId;

                window.parent.postMessage('post-selected', '*');

                setTimeout(function(){
                    document.getElementById('kc-accept').click();
                },100);
            });
        });
    </script>

    <script>
        function selectCustomer(f) {
            try {
                var xhr = new XMLHttpRequest();

                var actionUrl = "${url.loginAction}";
                actionUrl = actionUrl.replace(/&amp;/g, "&");

                xhr.open('POST', actionUrl, false);

                xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

                xhr.onloadend = function (response) {
                    window.parent.postMessage('post-selected', '*');
                    window.location.replace(response.currentTarget.responseURL);
                }

                xhr.send("tomsId=" + document.getElementById('tomsId').value +
                    "&postId=" + document.getElementById('postId').value);

            } catch (e) {
                window.parent.postMessage('post-selected-error', e);
                console.error(e);
            }

            return false;
        }
    </script>
</@layout.registrationLayout>