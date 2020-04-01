{
  "author": {
  <#if author?has_content>
    "name": "${author.effectiveName}",
    "avatar": "${author.user.effectiveAvatarUrl}"
  <#else>
    "name": "${user.id?c} (Has left the server)"
  </#if>
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  <#if message.content?has_content || message.embeds?size gt 0>
   "description": "${message.content}
   <#list message.embeds>
        Embeds:
        <#items as embed>
            Description: ${embed.description} <#if embed.imageUrl?has_content> ImageUrl: ${embed.imageUrl} </#if>
        </#items>
   </#list>
   ",
  </#if>
  <#if channel?has_content>
  "additionalMessage": "${starLevelEmote} ${starCount} ${channel.asMention} ID: ${message.messageId?c}",
  <#else>
  "additionalMessage": "${starLevelEmote} ${starCount} ${aChannel.id?c} ID: ${message.messageId?c}",
  </#if>
  <#if message.attachmentUrls?size gt 0>
  "imageUrl": "${message.attachmentUrls[0]}",
  </#if>
  "fields": [
    {
      "name": "Original",
      <#if channel?has_content>
      "value": "[${channel.name}](${message.messageUrl})"
      <#else>
      "value": "[${aChannel.id?c}](${message.messageUrl})"
      </#if>
    }
  ],
  "timeStamp": "${message.timeCreated}"
}