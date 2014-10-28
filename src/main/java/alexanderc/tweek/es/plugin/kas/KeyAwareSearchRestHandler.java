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
    public void handleRequest(final RestRequest request, final RestChannel channel) {
        String[] indices = Strings.splitStringByCommaToArray(request.param("index"));
        SearchRequest searchRequest = new SearchRequest(indices);

        String _key = request.param(KeyAwareSearchRestHandler.KEY_PARAM);
        String query = request.param(KeyAwareSearchRestHandler.QUERY_PARAM);
        Integer from = request.paramAsInt(KeyAwareSearchRestHandler.FROM_PARAM, 0);
        Integer size = request.paramAsInt(KeyAwareSearchRestHandler.SIZE_PARAM, KeyAwareSearchRestHandler.DEFAULT_SIZE);

        TermQueryBuilder keyQuery = QueryBuilders.termQuery(KeyAwareSearchRestHandler.KEY_PARAM, _key);
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

                channel.sendResponse(new StringRestResponse(OK, sb.toString()));
            }

            @Override
            public void onFailure(Throwable throwable) {
                channel.sendResponse(new StringRestResponse(OK, "Failed to search"));
            }
        });
    }
}
