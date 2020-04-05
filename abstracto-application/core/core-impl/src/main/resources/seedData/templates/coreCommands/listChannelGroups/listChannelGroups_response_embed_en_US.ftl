{
  "title": {
    "title": "Current configured channel groups"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "fields": [
     <#list groups as group>
        {
            "name": "${group.name}",
            "value": "
            <#list group.channels as channel>
                ${channel.discordChannel.asMention}
                <#sep>,
            </#list>
            "
        }<#sep>,
     </#list>
  ]
}