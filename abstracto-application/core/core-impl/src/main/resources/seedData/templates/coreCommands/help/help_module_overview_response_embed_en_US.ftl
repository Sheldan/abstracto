{
  "title": {
    "title": "<#include "help_modules_embed_title">"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "description": "
       <#list modules as module>
       <#include "help_module_embed_module_name">: **${module.info.name}**
       <#include "help_module_embed_module_description">: ${module.info.description}
       </#list>
  ",
  "footer": {
    "text": "<#include "help_modules_embed_footer_hint">"
  }
}