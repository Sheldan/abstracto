{
  "author": {
    "name": "${mutedUser.effectiveName}",
    "avatar":  "${mutedUser.user.effectiveAvatarUrl}"
  },
  "title": {
    "title": "<#include "mute_log_title">"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "fields": [
    {
      "name": "<#include "mute_log_muted_user_field_title">",
      "value": "${mutedUser.effectiveName} ${mutedUser.asMention} (${mutedUser.idLong?c})"
    },
    {
        "name": "<#include "mute_log_muting_user_field_title">",
        "value": "${mutingUser.effectiveName} ${mutingUser.asMention} (${mutingUser.idLong?c})"
    },
    {
        "name": "<#include "mute_log_mute_location_field_title">",
        "value": "[${messageChannel.name}](${message.jumpUrl})"
    },
    {
        "name": "<#include "mute_log_mute_reason_field_title">",
        "value": "${mute.reason}"
    },
    {
        "name": "<#include "mute_log_mute_duration_field_title">",
        "value": "${fmtDuration(muteDuration)}"
    },
    {
        "name": "<#include "mute_log_muted_until_field_title">",
        "value": "${formatDate(mute.muteTargetDate, "yyyy-MM-dd HH:mm:ss")}"
    }
  ],
  "footer": {
    "text": "<#include "mute_log_mute_id_footer"> #${mute.id}"
  },
  "timeStamp": "${mute.muteDate}"
}