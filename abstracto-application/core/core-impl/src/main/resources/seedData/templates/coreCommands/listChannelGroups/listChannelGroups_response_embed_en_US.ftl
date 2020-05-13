{
  "title": {
    "title": "<#include "listChannelGroups_embed_title">"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  <#if groups?size = 0>
  "description": "<#include "listChannelGroups_no_channel_group">",
  </#if>
  "fields": [
     <#list groups as group>
        {
            "name": "${group.name}",
            "value": "
            <#list group.channels as channel>${channel.discordChannel.asMention}<#sep>,</#list>
            "
        }<#sep>,
      <#else>
     </#list>
  ]
}