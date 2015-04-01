package alexanderc.tweek.es.plugin.kas.Response;

import org.elasticsearch.common.text.StringAndBytesText;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;

/**
 * Created by AlexanderC on 11/11/14.
 */
public class ExplainResponse extends BaseResponse {
    public ExplainResponse(final SearchSourceBuilder sourceBuilder) {
        this.setBytes(new StringAndBytesText(sourceBuilder.toString()).bytes());
        this.setStatus(RestStatus.OK);
    }
}
