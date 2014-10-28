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
import org.elasticsearch.rest.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestStatus.OK;

public class KeyAwareSearchRestHandler extends BaseRestHandler {

  @Inject
  public KeyAwareSearchRestHandler(Settings settings, Client client, RestController controller) {
    super(settings, client);

    controller.registerHandler(GET, "/_kas/{index}", this);
  }

  @Override
  public void handleRequest(final RestRequest request, final RestChannel channel) {
    String[] indices = Strings.splitStringByCommaToArray(request.param("index"));
    SearchRequest searchRequest = new SearchRequest(indices);

    String key = request.param("_key");

    QueryStringQueryBuilder qBuilder = QueryBuilders.queryString(request.param("q"));
    qBuilder.analyzer("standard");

    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    sourceBuilder.query(qBuilder);
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
