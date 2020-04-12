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
        Rank | Name | Experience | Level | Messages
       <#list userExperiences as user>
            <@userDisplay user=user />
       </#list>
       <@userDisplay user=userExecuting />
  "
}