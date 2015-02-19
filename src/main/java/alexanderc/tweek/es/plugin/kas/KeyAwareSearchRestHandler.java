package alexanderc.tweek.es.plugin.kas;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import static org.elasticsearch.rest.RestRequest.Method.GET;

/**
 * Created by AlexanderC on 10/28/14.
 */
public class KeyAwareSearchRestHandler extends BaseRestHandler {

    public static final String INDEX_PARAM = "index";
    public static final String QUERY_PARAM = "q";
    public static final String FROM_PARAM = "_offset";
    public static final String SIZE_PARAM = "_limit";
    public static final String KEY_PARAM = "_key";
    public static final String EXPLAIN_PARAM = "_explain";
    public static final String DEBUG_PARAM = "_debug";
    public static final Integer DEFAULT_SIZE = 10;
    public static final String KEY_FIELD = "_kas_key";
    public static final String ALL_INDEXES = "_all";

    @Inject
    public KeyAwareSearchRestHandler(Settings settings, RestController controller, Client client) {
        super(settings, controller, client);

        controller.registerHandler(GET, "/_kas/{" + INDEX_PARAM + "}", this);
    }

    @Override
    protected void handleRequest(RestRequest restRequest, final RestChannel restChannel, Client client) throws Exception {
        String[] indices = Strings.splitStringByCommaToArray(restRequest.param(INDEX_PARAM, ALL_INDEXES));
        SearchRequest searchRequest = new SearchRequest(indices);

        Boolean explain = restRequest.paramAsBoolean(EXPLAIN_PARAM, false);
        final Boolean debug = restRequest.paramAsBoolean(DEBUG_PARAM, false);
        String key = restRequest.param(KEY_PARAM, "");
        String query = restRequest.param(QUERY_PARAM, "");
        Integer from = restRequest.paramAsInt(FROM_PARAM, 0);
        from = from < 0 ? 0 : from;
        Integer size = restRequest.paramAsInt(SIZE_PARAM, DEFAULT_SIZE);
        size = size <= 0 ? DEFAULT_SIZE : size;

        TermFilterBuilder keyFilter = FilterBuilders.termFilter(KEY_FIELD, key);

        FilteredQueryBuilder filteredQuery = QueryBuilders.filteredQuery(
                query.isEmpty() ? QueryBuilders.matchAllQuery() : QueryBuilders.queryString(query),
                key.isEmpty() ? FilterBuilders.orFilter(
                        FilterBuilders.missingFilter(KEY_FIELD),
                        keyFilter
                ) : keyFilter
        );

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        sourceBuilder.fetchSource(null, KEY_FIELD);
        sourceBuilder.query(filteredQuery);
        sourceBuilder.from(from);
        sourceBuilder.size(size);
        searchRequest.extraSource(sourceBuilder);

        if(explain) {
            restChannel.sendResponse(new ExplainResponse(sourceBuilder));
        } else {
            // allow accessing from inner class
            final Integer resultFrom = from;
            final Integer resultSize = size;

            client.search(searchRequest, new ActionListener<SearchResponse>() {
                @Override
                public void onResponse(SearchResponse searchResponse) {
                    if(!searchResponse.status().equals(RestStatus.OK)) {
                        restChannel.sendResponse(new RestError("Search failed.", RestStatus.INTERNAL_SERVER_ERROR));
                    } else {
                        restChannel.sendResponse(new ResultResponse(searchResponse, resultFrom, resultSize));
                    }
                }

                @Override
                public void onFailure(Throwable throwable) {
                    restChannel.sendResponse(RestError.fromThrowable(throwable, debug));
                }
            });
        }
    }
}
