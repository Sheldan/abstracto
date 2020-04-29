{
  "title": {
    "title": "Warnings have been decayed"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "description": "<#list warnings as warning>
        <#if warning.warnedMember??>${warning.warnedMember.asMention} (${warning.warnedMember.id})<#else>${warning.warning.warnedUser.userReference.id?c}</#if> was warned on ${formatInstant(warning.warning.warnDate, "yyyy-MM-dd HH:mm:ss")}
        with reason `${warning.warning.reason}` by <#if warning.warningMember??>${warning.warningMember.asMention} (${warning.warningMember.id})<#else>${warning.warning.warningUser.userReference.id?c}</#if>

  <#else>
  No warnings to decay.
  </#list>"
}