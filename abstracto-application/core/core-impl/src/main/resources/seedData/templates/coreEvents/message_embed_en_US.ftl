{
  "author": {
    "name": "${author.effectiveName}",
    "avatar": "${author.user.avatarUrl}"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  <#if embeddedMessage.content?has_content >
   "description": "${embeddedMessage.content}",
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