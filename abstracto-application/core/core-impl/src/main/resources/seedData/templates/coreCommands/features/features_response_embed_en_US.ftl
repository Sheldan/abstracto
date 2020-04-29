{
  "title": {
    "title": "Currently available features"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "description": "
<#list features as feature>
${feature.featureFlag.enabled?string('✅', '❌')} **<#include "${feature.featureConfig.feature.key}_feature">** Key: `${feature.featureConfig.feature.key}`
</#list>
"
}