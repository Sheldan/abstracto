{
  "author": {
    "name": "${kickedUser.effectiveName}",
    "avatar":  "${kickedUser.user.effectiveAvatarUrl}"
  },
  "title": {
    "title": "<#include "kick_log_title">"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "fields": [
    {
      "name": "<#include "kick_log_kicked_user_field_title">",
      "value": "${kickedUser.effectiveName} ${kickedUser.asMention} (${kickedUser.idLong?c})"
    },
    {
        "name": "<#include "kick_log_kicking_user_field_title">",
        "value": "${kickingUser.effectiveName} ${kickingUser.asMention} (${kickingUser.idLong?c})"
    },
    {
        "name": "<#include "kick_log_jump_link_field_title">",
        "value": "[${messageChannel.name}](${message.jumpUrl})"
    },
    {
        "name": "<#include "kick_log_reason_field_title">",
        "value": "${reason}"
    }
  ]
}