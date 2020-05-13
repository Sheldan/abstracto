{
  "author": {
  <#if author?has_content>
    "name": "${author.effectiveName}",
    "avatar": "${author.user.effectiveAvatarUrl}"
  <#else>
    "name": "${user.id?c} (<#include "user_left_server">)"
  </#if>
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  }
  <#if message.content?has_content || message.embeds?size gt 0>
   ,"description": "${message.content}
   <#list message.embeds>
        <#include "starboard_post_embed_embeds_name">:
        <#items as embed>
            <#include "starboard_post_embed_description">: ${embed.description} <#if embed.imageUrl?has_content> <#include "starboard_post_embed_image_url">: ${embed.imageUrl} </#if>
        </#items>
   </#list>
   "
  </#if>
  <#assign emote>${starLevelEmote}</#assign>
  <#assign count>${starCount}</#assign>
  <#assign messageId>${message.messageId?c}</#assign>
  <#if channel?has_content>
  <#assign channelMention>${channel.asMention}</#assign>
  ,"additionalMessage": "<#include "starboard_post_embed_additional_message">"
  <#else>
  <#assign channelMention>${aChannel.id?c}</#assign>
  ,"additionalMessage": "<#include "starboard_post_embed_additional_message">"
  </#if>
  <#if message.attachmentUrls?size gt 0>
  ,"imageUrl": "${message.attachmentUrls[0]}"
  </#if>
  ,"fields": [
    {
      "name": "<#include "starboard_post_embed_original_field_title">"
      <#if channel?has_content>
      ,"value": "[${channel.name}](${message.messageUrl})"
      <#else>
      ,"value": "[${aChannel.id?c}](${message.messageUrl})"
      </#if>
    }
  ],
  "timeStamp": "${message.timeCreated}"
}