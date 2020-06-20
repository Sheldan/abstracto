{
  "author": {
    "name": "${bannedUser.effectiveName}",
    "avatar":  "${bannedUser.user.effectiveAvatarUrl}"
  },
  "title": {
    "title": "<#include "ban_log_title">"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "fields": [
    {
      "name": "<#include "ban_log_banned_user_field_title">",
      "value": "${bannedUser.effectiveName} ${bannedUser.asMention} (${bannedUser.idLong?c})"
    },
    {
        "name": "<#include "ban_log_banning_user_field_title">",
        "value": "${banningUser.effectiveName} ${banningUser.asMention} (${banningUser.idLong?c})"
    },
    {
        "name": "<#include "ban_log_jump_link_field_title">",
        "value": "[${messageChannel.name}](${message.jumpUrl})"
    },
    {
        "name": "<#include "ban_log_reason_field_title">",
        "value": "${reason}"
    }
  ]
}