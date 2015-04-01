package alexanderc.tweek.es.plugin.kas;

import alexanderc.tweek.es.plugin.kas.Response.ExplainResponse;
import alexanderc.tweek.es.plugin.kas.Response.RestErrorResponse;
import alexanderc.tweek.es.plugin.kas.Response.ResultResponse;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import alexanderc.tweek.es.plugin.kas.Exception.Exception;

import static org.elasticsearch.rest.RestRequest.Method.GET;

/**
 * Created by AlexanderC on 10/28/14.
 *
 * Currently implemented:
 *  q = _filter
 *  sort = _sort
 *  size = _limit
 *  from = _offset
 *
 * @see http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-uri-request.html
 *
 * @example http://localhost:9200/_kas/search/
 *              ?_fields=username,company
 *              &_limit=5
 *              &_offset=1
 *              &_filter=username:eistrati
 *              &_terms=workforceid:243468
 *              &_sort=+employee.workforceid,-employee.company_number
 *              &_token=65a5f38bf80914019b55a03f78c4
 *              &_explain
 *              &_debug
 */
public class KeyAwareSearchRestHandler extends BaseRestHandler {

    // _filter=name:Alex,surname:C - Set matching phrase (see Query String Query Syntax)
    // examples: http://www.elastic.co/guide/en/elasticsearch/reference/1.x/query-dsl-query-string-query.html
    public static final String QUERY_PARAM = "_filter";
    // _terms=type:article,author:john
    // examples: http://www.elastic.co/guide/en/elasticsearch/guide/current/_finding_exact_values.html
    public static final String TERMS_PARAM = "_terms";
    // _sort=+likes_total,-inactivity_count (order is important) - Sort desc.(-) or asc.(+), default asc.
    public static final String SORT_PARAM = "_sort";
    // _offset=10 - Set offset of the result (starting from ...)
    public static final String FROM_PARAM = "_offset";
    // _limit=50 - Set max results per page
    public static final String SIZE_PARAM = "_limit";
    // _token=13946|eistrati|e343d3aab6339f5kjshbdgi87gsghkge87a167c421c2d047ac551 - Auth token used
    public static final String KEY_PARAM = "_token";
    // _fields - Specify certain fields to be selected
    public static final String FIELDS_PARAM = "_fields";
    // _explain - Dumps source builder
    public static final String EXPLAIN_PARAM = "_explain";
    // _debug - Dumps debug trace on error
    public static final String DEBUG_PARAM = "_debug";

    public static final String INDEX_KEY = "index";
    public static final Integer DEFAULT_SIZE = 10;
    public static final String ALL_INDEXES = "_all";

    @Inject
    public KeyAwareSearchRestHandler(Settings settings, RestController controller, Client client) {
        super(settings, controller, client);

        controller.registerHandler(GET, "/_kas/{" + INDEX_KEY + "}", this);
    }

    @Override
    protected void handleRequest(RestRequest restRequest, final RestChannel restChannel, Client client) throws Exception {
        String[] indices = Strings.splitStringByCommaToArray(restRequest.param(INDEX_KEY, ALL_INDEXES));
        SearchRequest searchRequest = new SearchRequest(indices);

        Boolean explain = restRequest.paramAsBoolean(EXPLAIN_PARAM, false);
        final Boolean debug = restRequest.paramAsBoolean(DEBUG_PARAM, false);
        String fields = restRequest.param(FIELDS_PARAM, "");
        String key = restRequest.param(KEY_PARAM, "");
        String query = restRequest.param(QUERY_PARAM, "");
        String sort = restRequest.param(SORT_PARAM, "");
        String terms = restRequest.param(TERMS_PARAM, "");
        Integer from = restRequest.paramAsInt(FROM_PARAM, 0);
        from = from < 0 ? 0 : from;
        Integer size = restRequest.paramAsInt(SIZE_PARAM, DEFAULT_SIZE);
        size = size <= 0 ? DEFAULT_SIZE : size;

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        try {
            SourceBuilder sourceBuilder = new SourceBuilder(searchSourceBuilder, key);
            sourceBuilder.setFrom(from);
            sourceBuilder.setSize(size);

            if(!sort.isEmpty()) {
                sourceBuilder.addSort(sort);
            }

            if(!terms.isEmpty()) {
                sourceBuilder.addTerms(terms);
            }

            if(!fields.isEmpty()) {
                sourceBuilder.addFields(fields);
            }

            if(!query.isEmpty()) {
                sourceBuilder.setQuery(query);
            }

            searchSourceBuilder = sourceBuilder.build();
        } catch(Exception e) {
            restChannel.sendResponse(new RestErrorResponse(e.getMessage(), RestStatus.INTERNAL_SERVER_ERROR));
            return;
        }

        if(explain) {
            restChannel.sendResponse(new ExplainResponse(searchSourceBuilder));
            return;
        }

        searchRequest.extraSource(searchSourceBuilder);

        final Integer resultFrom = from;
        final Integer resultSize = size;

        client.search(searchRequest, new ActionListener<SearchResponse>() {
            @Override
            public void onResponse(SearchResponse searchResponse) {
                if(!searchResponse.status().equals(RestStatus.OK)) {
                    restChannel.sendResponse(new RestErrorResponse("Search failed", RestStatus.INTERNAL_SERVER_ERROR));
                } else {
                    restChannel.sendResponse(new ResultResponse(searchResponse, resultFrom, resultSize));
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                restChannel.sendResponse(RestErrorResponse.fromThrowable(throwable, debug));
            }
        });
    }
}
