<#list actionConfigs as actionConfig>
<#assign param=actionConfig.templateModel>
<#include "${actionConfig.templateName}">

</#list>