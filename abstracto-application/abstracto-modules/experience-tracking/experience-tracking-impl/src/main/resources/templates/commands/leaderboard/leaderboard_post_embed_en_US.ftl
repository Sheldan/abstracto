{
<#macro userDisplay user>
  ${user.rank} ${user.member.effectiveName} ${user.experience.experience} ${user.experience.currentLevel.level} ${user.experience.messageCount}
</#macro>
  "author": {
    "name": "${member.effectiveName}",
    "avatar":  "${member.user.effectiveAvatarUrl}"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "description": "
        <#include "leaderboard_rank_column"> | <#include "leaderboard_name_column"> | <#include "leaderboard_experience_column"> | <#include "leaderboard_level_column"> | <#include "leaderboard_messages_column">
       <#list userExperiences as user>
            <@userDisplay user=user />
       </#list>
       <@userDisplay user=userExecuting />
  "
}