{
<#assign name>${module.moduleInterface.info.name}</#assign>
  "title": {
    "title": "<#include "help_module_embed_title">"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "description": "
       <#include "help_module_embed_module_name">: **${module.moduleInterface.info.name}**
       <#include "help_module_embed_module_description">: ${module.moduleInterface.info.description}
       <#include "help_module_embed_commands">:
       <#list module.commands as command>`${command.configuration.name}`<#sep>, </#list>
       <#if module.subModules??>
       <#include "help_module_embed_sub_modules">: <#list module.subModules as module>`${module.info.name}`<#sep>, </#list>
       </#if>
  ",
  "footer": {
       "text": "<#include "help_command_embed_hint_footer">"
   }
}