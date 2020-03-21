{
  "author": {
    "name": "${suggester.effectiveName}",
    "avatar":  "${suggester.user.effectiveAvatarUrl}"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  <#if suggestion.state = "ACCEPTED">
    "description": "~~${text}~~ \n✅ ${reason} - Accepted by ${member.effectiveName}",
  <#elseif suggestion.state = "REJECTED">
    "description": "~~${text}~~ \n❌ ${reason} - Rejected by ${member.effectiveName}",
  <#else>
    "description": "${text}",
  </#if>
  <#if suggestion.state = "ACCEPTED" || suggestion.state = "REJECTED">
  "fields": [
    {
        "name": "Link",
        "value": "[Jump](${originalMessageUrl})"
    }
  ],
  </#if>
  "footer": {
    "text": "Suggestion #${suggestion.id}"
  }
}