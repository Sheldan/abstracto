{
  "author": {
    "name": "${mutedUser.effectiveName}",
    "avatar":  "${mutedUser.user.effectiveAvatarUrl}"
  },
  "title": {
    "title": "User has been muted"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "fields": [
    {
      "name": "Muted User",
      "value": "${mutedUser.effectiveName} ${mutedUser.asMention} (${mutedUser.idLong?c})"
    },
    {
        "name": "Muted by",
        "value": "${mutingUser.effectiveName} ${mutingUser.asMention} (${mutingUser.idLong?c})"
    },
    {
        "name": "Location of the mute",
        "value": "[${messageChannel.name}](${message.jumpUrl})"
    },
    {
        "name": "Reason",
        "value": "${mute.reason}"
    },
    {
        "name": "Muted for",
        "value": "${fmtDuration(muteDuration)}"
    },
    {
        "name": "Muted until",
        "value": "${formatInstant(mute.muteTargetDate, "yyyy-MM-dd HH:mm:ss")}"
    }
  ],
  "footer": {
    "text": "Mute #${mute.id}"
  },
  "timeStamp": "${mute.muteDate}"
}