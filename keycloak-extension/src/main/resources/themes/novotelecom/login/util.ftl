<#function if cond then else="">
    <#if cond>
        <#return then>
    <#else>
        <#return else>
    </#if>
</#function>

<#macro dump_keys object prefix="" level=0>
    <#if object?? && level < 5>
        <#if object?is_hash_ex>
            <#attempt>
                <#list object?keys as key>
                    ${prefix}.${key}
                    <@dump_keys object=object[key]!"" prefix=prefix+key level=level+1/>
                </#list>
                <#recover>
            </#attempt>
        </#if>
    </#if>
</#macro>

<#macro dump_data_model_keys>
    <#list .data_model?keys as key>
        ${key}
        <@dump_keys object=.data_model[key]!"" prefix=key/>
    </#list>
</#macro>
