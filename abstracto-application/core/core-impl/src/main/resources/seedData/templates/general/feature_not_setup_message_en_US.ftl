<#assign featureKey><#include "${featureTemplate}"></#assign><#include "feature_not_setup_message_text">

<#list errors as error>
<#include "${error.templateName}">

</#list>