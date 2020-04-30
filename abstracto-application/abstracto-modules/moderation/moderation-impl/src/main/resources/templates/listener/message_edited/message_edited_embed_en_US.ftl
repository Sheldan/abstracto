{
  "author": {
    "name": "${member.effectiveName}#${member.user.discriminator}",
    "avatar":  "${member.user.effectiveAvatarUrl}"
  },
  "description": "<#include "messageEdited_description_text">",
  "color" : {
    "r": 200,
    "g": 0,
    "b": 0
  },
  "fields": [
    {
      "name": "<#include "messageEdited_original_message_field_title">",
      "value": "${messageBefore.content}"
    },
    {
          "name": "<#include "messageEdited_new_message_field_title">",
          "value": "${messageAfter.contentRaw}"
    },
    {
        "name": "<#include "messageEdited_link_field_title">",
        "value": "[${messageChannel.name}](${messageBefore.messageUrl})"
    }
  ]
}