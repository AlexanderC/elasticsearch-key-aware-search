package alexanderc.tweek.es.plugin.kas;

import org.elasticsearch.common.text.StringAndBytesText;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.rest.*;

/**
 * Created by AlexanderC on 10/28/14.
 */
public class RestError extends BaseResponse {
    public RestError(final String message, RestStatus status) {
        StringBuilder sb = new StringBuilder();

        String codePrefix = "2";

        if (500 <= status.getStatus() && status.getStatus() <= 599) {
            codePrefix = "3";
        } else if (400 <= status.getStatus() && status.getStatus() <= 499) {
            codePrefix = "4";
        }

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
        StringBuilder error = new StringBuilder();

        if(debug) {
            error.append(throwable.toString());
            error.append('\n');

            for (StackTraceElement traceElement : throwable.getStackTrace()) {
                error.append('\n');
                error.append("[");
                error.append(traceElement.getFileName());
                error.append(":");
                error.append(traceElement.getLineNumber());
                error.append("] ");
                error.append(traceElement.getClassName());
                error.append(".");
                error.append(traceElement.getMethodName());
                error.append("()");
            }
        } else {
            error.append(throwable.getMessage());
        }

        RestStatus restStatus = RestStatus.INTERNAL_SERVER_ERROR;

        if(throwable instanceof ElasticsearchException) {
            restStatus = ((ElasticsearchException) throwable).status();
        }

        return new RestError(error.toString(), restStatus);
    }
}
