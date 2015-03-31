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
import org.elasticsearch.search.fetch.source.FetchSourceContext;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import java.util.ArrayList;
import java.util.List;

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
    public static final String KEY_FIELD = "_kas_key";
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
        String fields = restRequest.param(FIELDS_PARAM, "").trim();
        String key = restRequest.param(KEY_PARAM, "").trim();
        String query = restRequest.param(QUERY_PARAM, "").trim();
        String sort = restRequest.param(SORT_PARAM, "").trim();
        String terms = restRequest.param(TERMS_PARAM, "").trim();
        Integer from = restRequest.paramAsInt(FROM_PARAM, 0);
        from = from < 0 ? 0 : from;
        Integer size = restRequest.paramAsInt(SIZE_PARAM, DEFAULT_SIZE);
        size = size <= 0 ? DEFAULT_SIZE : size;

        List<TermsFilterBuilder> termsFilterVector = new ArrayList<TermsFilterBuilder>();

        if(!terms.isEmpty()) {
            terms = terms.replaceAll(",+", ",");
            String[] termsList = terms.split(",");

            for(String termItem : termsList) {
                Integer delimiterPos = termItem.indexOf(':');

                if(-1 == delimiterPos) {
                    restChannel.sendResponse(new RestError(
                            "Missing term delimiter in " + termItem,
                            RestStatus.UNPROCESSABLE_ENTITY
                    ));
                    return;
                }

                String termField = termItem.substring(0, delimiterPos);
                String termValue = termItem.substring(delimiterPos + 1);

                if(termField.equalsIgnoreCase(KEY_FIELD)) {
                    restChannel.sendResponse(new RestError(
                            "Searching for an internal term is forbidden",
                            RestStatus.NOT_ACCEPTABLE
                    ));
                    return;
                }

                if(0 == termValue.indexOf('(') && termValue.length() - 1 == termValue.lastIndexOf(')')) {
                    termValue = termValue.substring(1, termValue.length() - 1);

                    String[] termValuesVector = termValue.split("\\|");

                    List<String> cleanTermsFilterVector = new ArrayList<String>();

                    for(String termValueItem : termValuesVector) {
                        cleanTermsFilterVector.add(termValueItem.replaceAll("^\"(.+)\"$", "$1"));
                    }

                    termsFilterVector.add(FilterBuilders.inFilter(
                            termField,
                            cleanTermsFilterVector.toArray(new String[cleanTermsFilterVector.size()])
                    ));
                } else {
                    termValue = termValue.replaceAll("^\"(.+)\"$", "$1");

                    termsFilterVector.add(FilterBuilders.termsFilter(termField, termValue));
                }
            }
        }

        OrFilterBuilder searchKeyBuilder = FilterBuilders.orFilter(
                FilterBuilders.missingFilter(KEY_FIELD),
                FilterBuilders.termFilter(KEY_FIELD, ""),
                FilterBuilders.termFilter(KEY_FIELD, key)
        );

        FilteredQueryBuilder filteredQuery = QueryBuilders.filteredQuery(
                query.isEmpty() ? QueryBuilders.matchAllQuery() : QueryBuilders.queryString(query),
                termsFilterVector.isEmpty() ? searchKeyBuilder : FilterBuilders.andFilter(
                        searchKeyBuilder,
                        FilterBuilders.andFilter(
                                termsFilterVector.toArray(new TermsFilterBuilder[termsFilterVector.size()])
                        )
                )
        );

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        FetchSourceContext sourceContext = new FetchSourceContext(null, KEY_FIELD);

        if(!fields.isEmpty()) {
            fields = fields.replaceAll(",+", ",");
            String[] fieldsList = fields.split(",");

            sourceContext.includes(fieldsList);
        }

        if(!sort.isEmpty()) {
            sort = sort.replaceAll(",+", ",");
            String[] sortParts = sort.split(",");

            for(String sortField : sortParts) {
                SortOrder sortOrder = SortOrder.ASC;

                sortField = sortField.replaceAll("[^a-zA-Z0-9_\\-\\.]+", "");

                if(0 == sortField.indexOf('-')) {
                    sortField = sortField.substring(1);
                    sortOrder = SortOrder.DESC;
                }

                sourceBuilder.sort(SortBuilders.fieldSort(sortField).order(sortOrder));
            }
        }

        sourceBuilder.fetchSource(sourceContext);
        sourceBuilder.query(filteredQuery);
        sourceBuilder.from(from);
        sourceBuilder.size(size);

        searchRequest.extraSource(sourceBuilder);

        if(explain) {
            restChannel.sendResponse(new ExplainResponse(sourceBuilder));
            return;
        }

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
