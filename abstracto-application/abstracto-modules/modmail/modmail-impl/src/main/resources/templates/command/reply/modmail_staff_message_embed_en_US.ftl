{
  "author": {
    "name": "${moderator.effectiveName}",
    "avatar": "${moderator.user.effectiveAvatarUrl}"
  },
  "title": {
    "title": "<#include "modmail_thread_staff_message_title">"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  }
  <#if text?has_content>
   , "description": "${text}"
  </#if>
   <#if postedMessage.attachments?size gt 0>
   ,"imageUrl": "${postedMessage.attachments[0].proxyUrl}"
   </#if>
}