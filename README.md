elasticsearch-key-aware-search [es-kas]
=======================================

Sometimes we need to enforce the users to use only owned records- this plugin is what you're looking for.

It also secures ES by allowing only GET method and queries without aggregations and other advanced things (except dynamic scripting that should be disabled manually).

### Request Format

All the entries would be filtered by the `_key` parameter provided using the `_kas_key` field from mapping...

`<SERVER_PATH>`/_kas/`<INDEX_NAME>`?_key=`<KEY_STRING>`[&_fields=`<FIELDS_STRING>`&_limit=`<SIZE_INTEGER>`&_offset=`<OFFSET_INTEGER>`&_sort=`+|-<SORT_FIELD>`&_filter=`<QUERY_STRING>`&_terms=`<TERMS_STRING>`][&_explain&_debug]

### Options

- `_filter` (aka `q`) - by default query is `match_all` (original syntax)
- `_limit` (aka `size`) - by default query is 10
- `_offset` (aka `from`) - by default is 0
- `_sort` (aka `sort`) - by default none (comma separated, `+|-field` syntax)
- `_explain` - dump native query
- `_debug` - adds exceptions stack trace to the error response
- `_fields` - define the fields to be returned in response object (by default all).
- `_terms` - add terms to search for

### Filtering results

`_filter` parameter is a proxy for native [query string query](http://www.elastic.co/guide/en/elasticsearch/reference/1.x/query-dsl-query-string-query.html)

### Finding exact values

In order to find exact values `_terms` parameter should be user ([more info](http://www.elastic.co/guide/en/elasticsearch/guide/current/_finding_exact_values.html))

Syntax:
- `_terms=uid:4146421` - find user with `uuid` equals to `4146421`
- `_terms=uid:(4146421|8768764)` - find user with `uuid` equals to `4146421` or `8768764`
- `_terms=uid:4146421,username=(john|james)` - find user with `uuid` equals to `4146421` and `username` equals to `john` or `james`

Be aware: You could not search for `_kas_key` field!

### Be aware!
    
- `GET` is the only allowed method
- `_kas_key` field MUST be `not_analyzed`

### Requirements

- ElasticSearch 1.4.2
- Apache Lucene 4.9.x (mainly depends on ES version)

### Installation instructions

`bin/plugin --url https://github.com/AlexanderC/elasticsearch-key-aware-search/blob/master/builds/es-kas-1.0.0b-plugin.zip?raw=true --install es-kas --verbose`

To uninstall it run: `bin/plugin --remove es-kas`

### Changelog

- v0.1-SNAPSHOT
    - Created skeleton app
    - implemented basic features

- v1.0b
    - Implemented terms search
    - Plugin refactored