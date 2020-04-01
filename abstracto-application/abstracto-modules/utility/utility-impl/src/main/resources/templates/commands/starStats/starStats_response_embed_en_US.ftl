{
  "title": {
    "title": "Server starboard stats"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "description": "${starredMessages} starred messages with ${totalStars} stars in total",
  "fields": [
      {
            "name": "Top starred posts",
            "value": "
                <#list topPosts as post>
                    ${badgeEmotes[post?index]} - ${post.starCount} :star: [Jump!](${post.messageUrl})
                <#else>
                    No starred messages.
                </#list>
            "
      },
      {
            "name": "Top starrer",
            "value": "
                <#list starGiver as starrer>
                    <#if starrer.member?has_content>
                        ${badgeEmotes[starrer?index]} - ${starrer.starCount} :star: ${starrer.member.asMention}
                    <#else>
                        ${badgeEmotes[starrer?index]} - ${starrer.starCount} :star: ${starrer.user.id?c} (Left the guild)
                    </#if>
                 <#else>
                    No starred messages.
                </#list>
            "
      },
     {
             "name": "Top star receiver",
             "value": "
                <#list starReceiver as starred>
                    <#if starred.member?has_content>
                        ${badgeEmotes[starred?index]} - ${starred.starCount} :star: ${starred.member.asMention}
                    <#else>
                        ${badgeEmotes[starred?index]} - ${starred.starCount} :star: ${starred.user.id?c} (Left the guild)
                    </#if>
                <#else>
                  No starred messages.
                </#list>
             "
     }
  ]
}