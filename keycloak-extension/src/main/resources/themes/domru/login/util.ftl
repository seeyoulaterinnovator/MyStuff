<#function if cond then else="">
    <#if cond>
        <#return then>
    <#else>
        <#return else>
    </#if>
</#function>

<#macro dump_keys object prefix="">
    <#if object??>
        <#if object?is_hash_ex>
            <#list object?keys as key>
                ${prefix}.${key}
                <@dump_keys object=.data_model[key]!"" prefix=prefix+key/>
            </#list>
        </#if>
    </#if>
</#macro>

<#macro dump_data_model_keys>
    <#list .data_model?keys as key>
        ${key}
        <@dump_keys object=.data_model[key]!"" prefix=key/>
    </#list>
</#macro>
