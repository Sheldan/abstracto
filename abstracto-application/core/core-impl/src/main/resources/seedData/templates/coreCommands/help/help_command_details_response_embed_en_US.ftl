{
  "title": {
    "title": "<#include "help_command_embed_title">"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "description": "<#include "help_command_embed_command_name">: **${command.name}**
<#include "help_command_embed_command_description">: <#if command.templated> <#include "${command.name}_description"> <#else> ${command.description} </#if>

<#if command.help??>
<#if command.help.templated>
<#include "help_command_embed_command_usage">: `<#include "${command.name}_usage">`
<#include "help_command_embed_command_detailed_help">: <#include "${command.name}_long_help">
<#if command.help.hasExample>
<#include "help_command_embed_command_example">: <#include "${command.name}_example">
</#if>
<#else>
<#include "help_command_embed_command_usage">: `${command.help.usage}`
<#include "help_command_embed_command_detailed_help">: ${command.help.longHelp}
<#if command.help.hasExample>
<#include "help_command_embed_command_example">:${command.help.example}
</#if>
</#if>
<#if command.aliases?? && command.aliases?size gt 0>
<#include "help_command_embed_command_aliases">: `${command.aliases?join("`, `")}`
</#if>
<#if restricted?? && restricted>
<#include "help_command_embed_command_executable_by">:<#if allowedRoles??> <#list allowedRoles as allowedRole> ${allowedRole.asMention}<#sep><#include "help_command_embed_or"><#else><#include "help_command_embed_command_executable_by_nobody"></#list> </#if>
<#if immuneRoles?? ><#include "help_command_embed_command_immune">: <#list immuneRoles as immuneRole> ${immuneRole.asMention}<#sep><#include "help_command_embed_or"><#else>None</#list> </#if>
<#else>
<#include "help_command_embed_command_not_restricted">
</#if>

</#if>
<#include "help_command_embed_command_parameters">:
<#if command.parameters??>
<#list command.parameters as parameter>
<#include "help_command_embed_command_description"> `${parameter.name}`: <#if parameter.templated?? && parameter.templated><#include "${command.name}_parameter_${parameter.name}"><#else>${(parameter.description)!""}</#if>
<#include "help_command_embed_command_optional"><#sep>

<#else>
<#include "help_command_embed_command_no_parameters">
</#list>
<#else>
<#include "help_command_embed_command_no_parameters">
</#if>
  "
}