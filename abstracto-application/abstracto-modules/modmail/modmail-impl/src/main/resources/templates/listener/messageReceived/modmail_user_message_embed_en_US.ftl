{
  "author": {
    "name": "${threadUser.member.effectiveName}",
    "avatar":  "${threadUser.member.user.effectiveAvatarUrl}"
  },
  "title": {
    "title": "<#include "modmail_thread_user_message_title">"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  <#if postedMessage.contentRaw?has_content>
    "description": "${postedMessage.contentRaw}"
  </#if>
   <#if postedMessage.attachments?size gt 0>
   ,"imageUrl": "${postedMessage.attachments[0].proxyUrl}"
   </#if>
   <#if subscribers?size gt 0>
   ,"additionalMessage": "<#list subscribers as subscriber>${subscriber.member.asMention}<#sep>,</#list>"
   </#if>
}