{
    <#assign id>${reminder.id}</#assign>
    <#assign messageUrl>${message.jumpUrl}</#assign>
  "author": {
    "name": "${member.effectiveName}",
    "avatar":  "${member.user.effectiveAvatarUrl}"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "description": "<#include "remind_reminding_description_text">",
  "additionalMessage": "${member.asMention}"
}