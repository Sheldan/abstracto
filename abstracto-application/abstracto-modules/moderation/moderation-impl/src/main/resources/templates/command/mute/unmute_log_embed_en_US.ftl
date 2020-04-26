{
  <#if unMutedUser?has_content>
    "author": {
        "name": "${unMutedUser.effectiveName}",
        "avatar":  "${unMutedUser.user.effectiveAvatarUrl}"
    },
  </#if>
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
      <#if unMutedUser?has_content>
        "value": "${unMutedUser.effectiveName} ${unMutedUser.asMention} (${unMutedUser.idLong?c})"
      <#else>
        "value": "User has left the server (${mute.mutedUser.userReference.id?c})"
      </#if>

    },
    {
        "name": "Muted by",
         <#if mutingUser?has_content>
            "value": "${mutingUser.effectiveName} ${mutingUser.asMention} (${mutingUser.idLong?c})"
         <#else>
                "value": "User has left the server (${mute.mutingUser.userReference.id?c})"
         </#if>
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
  },
  "timeStamp": "${unmuteDate}"
}