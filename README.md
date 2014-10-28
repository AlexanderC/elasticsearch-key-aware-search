elasticsearch-key-aware-search [es-kas]
=======================================

Sometimes we need to enforce the users to use only owned records- this plugin is what you're looking for.

It also secures ES by allowing only GET method and queries without aggregations and other advanced things (except dynamic scripting that should be disabled manually).

### Request Format

All the entries would be filtered using the `_key` provided using the `_kas_key` field...

`<SERVER_PATH>`/_kas/`<INDEX_NAME>`?_key=`<KEY_STRING>`[&size=`<SIZE_INTEGER>`&from=`<OFFSET_INTEGER>`&q=`<QUERY_STRING>`]

### Defaults

    'q' - by default query is 'match_all'
    'size' - by default query is 10
    'from' - by default is 0
    
### Be aware!
    
- `GET` is the only allowed method    
- `_kas_key` field MUST be `not_analyzed`


### Installation instructions

`bin/plugin --url file:%ROOT%/builds/es-kas-0.1-SNAPSHOT-plugin.zip --install es-kas --verbose`

To uninstall it run: `bin/plugin --remove es-kas`