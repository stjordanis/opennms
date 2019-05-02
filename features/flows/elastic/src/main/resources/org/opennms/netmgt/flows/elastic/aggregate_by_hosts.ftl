{
  "size": 0,
  "query": {
    "bool": {
      "filter": [
<#list filters as filter>${filter}<#sep>,</#list>
      ],
      "must": {
        "term": {
          "${field?json_string}": "${cidr?json_string}"
        }
      }
    }
  },
  "aggs": {
    "grouped_by": {
      "terms": {
        "field": "${field?json_string}",
        "size": ${N?long?c},
        "order": {
          "_key": "asc"
        }
      }
    }
  }
}
