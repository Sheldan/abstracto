{
  "title": {
    "title": "<#include "posttarget_embed_title">"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "description": "
<#list postTargets as postTarget>
<#assign postTargetName>${postTarget.postTarget.name}</#assign>
<#assign channelMention><#if postTarget.channel?has_content>${postTarget.channel.asMention}<#else><#include "posttarget_no_channel"></#if></#assign>
<#include "posttarget_post_target_text">
<#else>
<#include "posttarget_no_post_targets_found">
</#list>
"
}