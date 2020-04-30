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
        <#include "message_embed_embed_embeds_name">:
        <#items as embed>
            <#include "message_embed_embed_description">: <#if embed.description?has_content >${embed.description}</#if> <#if embed.imageUrl?has_content> <#include "message_embed_embed_image_url">: ${embed.imageUrl} </#if>
        </#items>
   </#list>
   ",
  </#if>
  <#if embeddedMessage.attachmentUrls?size gt 0>
  "imageUrl": "${embeddedMessage.attachmentUrls[0]}",
  </#if>
  "fields": [
    {
      "name": "<#include "message_embed_embed_quoted_by_field_title">",
      <#assign user>${embeddingUser.asMention}</#assign>
      <#assign channelName>${sourceChannel.name}</#assign>
      <#assign messageLink>${embeddedMessage.messageUrl}</#assign>
      "value": "<#include "message_embed_embed_quoted_by_field_value">"
    }
  ],
  "timeStamp": "${embeddedMessage.timeCreated}"
}