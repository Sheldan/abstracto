{
  "author": {
    "name": "${unMutedUser.effectiveName}",
    "avatar":  "${unMutedUser.user.effectiveAvatarUrl}"
  },
  "title": {
    "title": "User has been unmuted"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "fields": [
    {
      "name": "Unmuted User",
      "value": "${unMutedUser.effectiveName} ${unMutedUser.asMention} (${unMutedUser.idLong?c})"
    },
    {
        "name": "Muted by",
        "value": "${mutingUser.effectiveName} ${mutingUser.asMention} (${mutingUser.idLong?c})"
    },
    {
        "name": "Location of the mute",
        "value": "[Link](${messageUrl})"
    },
    {
        "name": "Muted since",
        "value": "${formatInstant(mute.muteDate, "yyyy-MM-dd HH:mm:ss")}"
    },
    {
        "name": "Muted for",
        "value": "${fmtDuration(muteDuration)}"
    },
    {
        "name": "Reason",
        "value": "${mute.reason}"
    }
  ],
  "footer": {
    "text": "Mute #${mute.id}"
  }
}