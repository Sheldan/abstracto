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
        "name": "<#include "rank_xp_field_title">",
        "value": "${rankUser.experience.experience}",
        "inline": "true"
    },
    {
        "name": "<#include "rank_level_field_title">",
        "value": "${rankUser.experience.currentLevel.level}",
        "inline": "true"
    },
    {
        "name": "<#include "rank_messages_field_title">",
        "value": "${rankUser.experience.messageCount}",
        "inline": "true"
    },
    {
        "name": "<#include "rank_to_next_level_field_title">",
        "value": "${experienceToNextLevel}",
        "inline": "true"
    },
    {
        "name": "<#include "rank_rank_field_title">",
        "value": "${rankUser.rank}",
        "inline": "true"
    }
  ]
}