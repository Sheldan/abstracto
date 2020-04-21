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
  "description": "You wanted to be reminded.",
  "fields": [
    {
        "name": "Duration",
        "value": "${fmtDuration(duration)}"
    },
    {
        "name": "Note",
        "value": "${reminder.text}"
    },
    {
        "name": "Link",
        "value": "[Jump!](${messageUrl})"
    }
  ],
  "additionalMessage": "${member.asMention}"
}