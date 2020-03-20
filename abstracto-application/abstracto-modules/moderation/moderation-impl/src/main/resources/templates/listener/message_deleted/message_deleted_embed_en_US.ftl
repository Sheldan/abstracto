{
  "author": {
    "name": "${member.effectiveName}#${member.user.discriminator}",
    "avatar":  "${member.user.effectiveAvatarUrl}"
  },
  "description": "Message from ${member.effectiveName}#${member.user.discriminator} (${member.idLong?c}) removed in ${textChannel.asMention}",
  "color" : {
    "r": 200,
    "g": 0,
    "b": 0
  },
  "fields": [
    {
      "name": ":x: Original Message: ",
      "value": "${message.contentRaw}"
    },
    {
        "name": "Link",
        "value": "[${textChannel.name}](${message.jumpUrl})"
    }
  ]
}