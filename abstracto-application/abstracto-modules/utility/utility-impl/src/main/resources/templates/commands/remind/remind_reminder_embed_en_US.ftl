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
  "description": "You wanted to be reminded of: '${reminder.text}'. Original message was [here](${messageUrl}).",
  "additionalMessage": "${member.asMention}"
}