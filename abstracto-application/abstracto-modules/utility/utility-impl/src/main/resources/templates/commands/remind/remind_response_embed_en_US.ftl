{
  "author": {
    "name": "${member.effectiveName}",
    "avatar":  "${member.user.effectiveAvatarUrl}"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "description": "Scheduled reminder ${reminder.id} to remind you of [this](${message.jumpUrl})",
  "additionalMessage": "${member.asMention}"
}