{
  "author": {
    "name": "${author.member.effectiveName}",
    "avatar":  "${author.member.user.effectiveAvatarUrl}"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  }
  <#if message.embeds[0].description?has_content>
  ,"description": "${message.embeds[0].description}"
  </#if>
   <#if message.attachments?size gt 0>
     ,"imageUrl": "${message.embeds[0].image.proxyUrl}"
   </#if>
   <#if modMailMessage.anonymous>
   , "additionalMessage": "<#include "modmail_anonymous_message_note">"
   </#if>
   ,"timeStamp": "${message.timeCreated}"
}
