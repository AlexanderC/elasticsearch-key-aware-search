elasticsearch-key-aware-search [es-kas]
=======================================

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