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
    public ResultResponse(SearchResponse searchResponse) {
        Iterator<SearchHit> hits = searchResponse.getHits().iterator();

        String[] collection = Strings.EMPTY_ARRAY;

        while(hits.hasNext()) {
            collection = Strings.addStringToArray(collection, ((SearchHit) hits.next()).getSourceAsString());
        }

        StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append("\"total_hits\": ").append(searchResponse.getHits().getTotalHits()).append(",");
        sb.append("\"hits\": [");
        sb.append(Strings.arrayToCommaDelimitedString(collection));
        sb.append("]}");

        StringAndBytesText responseObject = new StringAndBytesText(sb.toString());

        this.setBytes(responseObject.bytes());
        this.setStatus(RestStatus.OK);
    }
}
