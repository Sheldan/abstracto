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
  "fields": [
    {
        "name": "XP",
        "value": "${rankUser.experience.experience}",
        "inline": "true"
    },
    {
        "name": "Level",
        "value": "${rankUser.experience.currentLevel.level}",
        "inline": "true"
    },
    {
        "name": "Messages",
        "value": "${rankUser.experience.messageCount}",
        "inline": "true"
    },
    {
        "name": "XP to next Level",
        "value": "${experienceToNextLevel}",
        "inline": "true"
    },
    {
        "name": "Rank",
        "value": "${rankUser.rank}",
        "inline": "true"
    }
  ]
}