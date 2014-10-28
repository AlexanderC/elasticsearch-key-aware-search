package alexanderc.tweek.es.plugin.kas;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.rest.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestStatus.OK;

public class KeyAwareSearchRestHandler extends BaseRestHandler {

    public static final String QUERY_PARAM = "q";
    public static final String FROM_PARAM = "from";
    public static final String SIZE_PARAM = "size";
    public static final String KEY_PARAM = "_key";
    public static final Integer DEFAULT_SIZE = 10;

    @Inject
    public KeyAwareSearchRestHandler(Settings settings, Client client, RestController controller) {
        super(settings, client);

        controller.registerHandler(GET, "/_kas/{index}", this);
    }

    @Override
    protected void handleRequest(RestRequest restRequest, final RestChannel restChannel, Client client) throws Exception {
        String[] indices = Strings.splitStringByCommaToArray(restRequest.param("index", "_all"));
        SearchRequest searchRequest = new SearchRequest(indices);

        String _key = restRequest.param(KEY_PARAM);
        String query = restRequest.param(QUERY_PARAM);
        Integer from = restRequest.paramAsInt(FROM_PARAM, 0);
        Integer size = restRequest.paramAsInt(SIZE_PARAM, DEFAULT_SIZE);

        if(_key.isEmpty()) {
            restChannel.sendResponse(new BytesRestResponse(OK,
                    "You MUST provide the key as '%' request parameter".replace("%", KEY_PARAM)
            ));
        } else {
            TermQueryBuilder keyQuery = QueryBuilders.termQuery(KEY_PARAM, _key);
            QueryStringQueryBuilder queryBuilder = QueryBuilders.queryString(query);

            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            //sourceBuilder.query(keyQuery);
            sourceBuilder.query(queryBuilder);
            sourceBuilder.from(from);
            sourceBuilder.size(size);

            searchRequest.extraSource(sourceBuilder);

            client.search(searchRequest, new ActionListener<SearchResponse>() {
                @Override
                public void onResponse(SearchResponse searchResponse) {
                    SearchHits hits = searchResponse.getHits();

                    StringBuilder sb = new StringBuilder();

                    sb.append("{");
                    sb.append("\"total_hits\": ").append(hits.getTotalHits()).append(",");
                    sb.append("\"hits\": [\n");

                    for (SearchHit hit : hits) {
                        sb.append(hit.sourceAsString()).append(",\n");
                    }

                    sb.append("]\n}");

                    restChannel.sendResponse(new BytesRestResponse(OK, sb.toString()));
                }

                @Override
                public void onFailure(Throwable throwable) {
                    restChannel.sendResponse(new BytesRestResponse(OK, throwable.getMessage()));
                }
            });
        }
    }
}
