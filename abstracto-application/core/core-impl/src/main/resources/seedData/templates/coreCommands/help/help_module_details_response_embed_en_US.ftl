{
  "title": {
    "title": "Help - Module ${module.moduleInterface.info.name} details"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "description": "
       Module name: **${module.moduleInterface.info.name}**
       Description: ${module.moduleInterface.info.description}
       Commands:
       <#list module.commands as command>`${command.configuration.name}`<#sep>, </#list>
       <#if module.subModules??>
       Submodules: <#list module.subModules as module>`${module.info.name}`<#sep>, </#list>
       </#if>
  ",
  "footer": {
       "text": "Use 'help <command name>' for a detailed overview of this command."
   }
}