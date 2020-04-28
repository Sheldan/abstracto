{
  "title": {
    "title": "Help - Module overview"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "description": "
       <#list modules as module>
       Module name: **${module.info.name}**
       Description: ${module.info.description}
       </#list>
  ",
  "footer": {
    "text": "Use 'help <module name>' for a list of commands of this module."
  }
}