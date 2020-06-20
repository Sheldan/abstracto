{
  "author": {
    "name": "${member.effectiveName}#${member.user.discriminator}",
    "avatar":  "${member.user.effectiveAvatarUrl}"
  },
  "description": "<#include "messageDeleted_description_text">",
  "color" : {
    "r": 200,
    "g": 0,
    "b": 0
  },
  "fields": [
    {
      "name": "<#include "messageDeleted_original_message_field_title">",
      "value": "${cachedMessage.content}"
    },
    {
        "name": "<#include "messageDeleted_message_link_field_title">",
        "value": "[${messageChannel.name}](${cachedMessage.messageUrl})"
    }
  ]
}