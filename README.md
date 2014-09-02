es-suche
========

An example ElasticSearch plugin implementation which performs search and expose a web service endpoint at "/suche?q=&lt;Query_String>"

Sept 2nd 2014: We developped this ES plugin for the arte RFP.

### Installation

```
bin/plugin --url https://github.com/Ubertweek/es-suche/releases/download/1.0-SNAPSHOT/es-suche-1.0-SNAPSHOT-plugin.zip --install es-suche
```

### Sample Request

```
curl "http://localhost:9200/video/suche?q=title_de:Kurz"
```

This should return all search hits.

### Request Format

`<SERVER_PATH>`/`<INDEX_NAME>`/suche?q=`<QUERY_STRING>`
