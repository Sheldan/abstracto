{
  "title": {
    "title": "<#include "starStats_response_title">"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "description": "<#include "startStats_description">",
  "fields": [
      {
            "name": "<#include "starStats_top_starred_field_title">",
            "value": "
                <#list topPosts as post>
                <#assign badge>${badgeEmotes[post?index]}</#assign><#assign count>${post.starCount}</assign><#assign link>${post.messageUrl}</#assign> <#include "starStats_starred_entry">
                <#else>
                    <#include "starStats_no_starred_messages">
                </#list>
            "
      },
      {
            "name": "<#include "starStats_top_starrer_field_title">",
            "value": "
                <#list starGiver as starrer>
                    <#assign badge>${badgeEmotes[starrer?index]}</#assign><#assign count>${starrer.starCount}</assign>
                    <#if starrer.member?has_content>
                    <#assign user>${starrer.member.asMention}</#assign> <#include "starStats_starrer_entry">
                    <#else>
                        <#assign user>${starrer.user.id?c}</#assign> <#include "starStats_starrer_entry_left_guild_en_US.ftl">
                    </#if>
                 <#else>
                     <#include "starStats_no_starred_messages">
                </#list>
            "
      },
     {
             "name": "<#include "starStats_top_receiver_field_title">",
             "value": "
                <#list starReceiver as starred>
                    <#assign badge>${badgeEmotes[starred?index]}</#assign><#assign count>${starred.starCount}</assign>
                    <#if starred.member?has_content>
                     <#assign user>${starred.member.asMention}</#assign> <#include "starStats_receiver_entry">
                    <#else>
                     <#assign user>${starred.user.id?c}</#assign> <#include "starStats_receiver_entry_left_guild">
                    </#if>
                <#else>
                   <#include "starStats_no_starred_messages">
                </#list>
             "
     }
  ]
}