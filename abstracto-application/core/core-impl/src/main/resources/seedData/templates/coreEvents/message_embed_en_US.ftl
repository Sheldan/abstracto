{
  "author": {
    "name": "${author.effectiveName}",
    "avatar": "${author.user.effectiveAvatarUrl}"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  <#if embeddedMessage.content?has_content || embeddedMessage.embeds?size gt 0>
   "description": "${embeddedMessage.content}
   <#list embeddedMessage.embeds>
        Embeds:
        <#items as embed>
            Description: ${embed.description} <#if embed.imageUrl?has_content> ImageUrl: ${embed.imageUrl} </#if>
        </#items>
   </#list>
   ",
  </#if>
  <#if embeddedMessage.attachmentUrls?size gt 0>
  "imageUrl": "${embeddedMessage.attachmentUrls[0]}",
  </#if>
  "fields": [
    {
      "name": "Quoted by",
      "value": "${embeddingUser.asMention} from [${sourceChannel.name}](${embeddedMessage.messageUrl})"
    }
  ],
  "timeStamp": "${embeddedMessage.timeCreated}"
}