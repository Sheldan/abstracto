{
  "author": {
    "name": "${threadUser.member.effectiveName}",
    "avatar":  "${threadUser.member.user.effectiveAvatarUrl}"
  },
  "title": {
    "title": "<#include "modmail_notification_message_title">"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  <#assign user>${threadUser.member.effectiveName}#${threadUser.member.user.discriminator}(${threadUser.member.user.id})</#assign>
  "description": "<#include "modmail_notification_message_description">"
  <#if roles?size gt 0>
  ,"additionalMessage": "<#list roles as role>${role.role.asMention}<#sep>,</#list>"
  </#if>
}