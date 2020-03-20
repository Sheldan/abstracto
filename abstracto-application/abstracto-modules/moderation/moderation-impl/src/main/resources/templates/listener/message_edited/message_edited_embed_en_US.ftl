{
  "author": {
    "name": "${member.effectiveName}#${member.user.discriminator}",
    "avatar":  "${member.user.effectiveAvatarUrl}"
  },
  "description": "Message from ${member.effectiveName}#${member.user.discriminator} (${member.idLong?c}) edited in ${textChannel.asMention}",
  "color" : {
    "r": 200,
    "g": 0,
    "b": 0
  },
  "fields": [
    {
      "name": "Original Message: ",
      "value": "${messageBefore.contentRaw}"
    },
    {
          "name": "New Message: ",
          "value": "${messageAfter.contentRaw}"
    },
    {
        "name": "Jump link",
        "value": "[${textChannel.name}](${messageBefore.jumpUrl})"
    }
  ]
}