package alexanderc.tweek.es.plugin.kas;

import org.elasticsearch.common.text.StringAndBytesText;
import org.elasticsearch.rest.*;

/**
 * Created by AlexanderC on 10/28/14.
 */
public class RestError extends BaseResponse {
    public RestError(final String message, RestStatus status) {
        StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append("\"error\":\"%\",".replace("%", message.replace("\"", "\\\"").replace("\n", "\\n")));
        sb.append("\"status\":").append(status.getStatus());
        sb.append("}");

        StringAndBytesText responseObject = new StringAndBytesText(sb.toString());

        this.setBytes(responseObject.bytes());
        this.setStatus(status);
    }
}
