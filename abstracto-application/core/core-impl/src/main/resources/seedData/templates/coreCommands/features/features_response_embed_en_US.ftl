{
  "title": {
    "title": "<#include "features_embed_title">"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "description": "
<#list features as feature>
${feature.featureFlag.enabled?string('✅', '❌')} **<#include "${feature.featureConfig.feature.key}_feature">** <#include "feature_embed_key">: `${feature.featureConfig.feature.key}`
</#list>
"
}