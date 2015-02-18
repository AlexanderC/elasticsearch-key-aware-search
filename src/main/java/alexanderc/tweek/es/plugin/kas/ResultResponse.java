package alexanderc.tweek.es.plugin.kas;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.text.StringAndBytesText;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import java.util.Iterator;

/**
 * Created by AlexanderC on 10/28/14.
 */
public class ResultResponse extends BaseResponse {
    public ResultResponse(final SearchResponse searchResponse, final Integer offset, final Integer limit) {
        Iterator<SearchHit> hits = searchResponse.getHits().iterator();

        StringBuilder collection = new StringBuilder();

        while(hits.hasNext()) {
            collection.append(((SearchHit) hits.next()).getSourceAsString());

            if(hits.hasNext()) {
                collection.append(',');
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append("\"pagination\":{\"count\":%1,\"limit\":%2,\"offset\":%3},"
                        .replace("%1", Long.toString(searchResponse.getHits().getTotalHits()))
                        .replace("%2", Integer.toString(limit))
                        .replace("%3", Integer.toString(offset)));
        sb.append("\"results\":[");
        sb.append(collection.toString());
        sb.append("]}");

        StringAndBytesText responseObject = new StringAndBytesText(sb.toString());

        this.setBytes(responseObject.bytes());
        this.setStatus(RestStatus.OK);
    }
}
