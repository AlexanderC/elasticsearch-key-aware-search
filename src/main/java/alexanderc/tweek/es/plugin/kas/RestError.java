package alexanderc.tweek.es.plugin.kas;

import org.elasticsearch.common.text.StringAndBytesText;
import org.elasticsearch.rest.*;

/**
 * Created by AlexanderC on 10/28/14.
 */
public class RestError extends BaseResponse {
    public RestError(final String message, RestStatus status) {
        StringBuilder sb = new StringBuilder();

        String codePrefix = Integer.toString(status.getStatus()).substring(0, 1);

        sb.append("{");
        sb.append("\"error\":true,");
        sb.append("\"message\":\"%1\",".replace("%1", message.replace("\"", "\\\"").replace("\n", "\\n")));
        sb.append("\"code\":").append(codePrefix).append(status.getStatus());
        sb.append("}");

        StringAndBytesText responseObject = new StringAndBytesText(sb.toString());

        this.setBytes(responseObject.bytes());
        this.setStatus(status);
    }

    public static RestError fromThrowable(Throwable throwable, Boolean debug) {
        String error = throwable.getMessage();

        if(debug) {
            error = throwable.toString();
        }

        return new RestError(error, RestStatus.INTERNAL_SERVER_ERROR);
    }
}
