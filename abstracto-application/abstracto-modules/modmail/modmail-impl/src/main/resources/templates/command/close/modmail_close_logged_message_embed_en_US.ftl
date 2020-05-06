{
  "author": {
    "name": "${author.member.effectiveName}",
    "avatar":  "${author.member.user.effectiveAvatarUrl}"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "description": "${message.embeds[0].description}"
   <#if message.attachments?size gt 0>
     ,"imageUrl": "${message.embeds[0].image.proxyUrl}"
   </#if>,
    "timeStamp": "${message.timeCreated}"
}
