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
        StringBuilder error = new StringBuilder();

        if(debug) {
            error.append(throwable.toString());
            error.append('\n');

            for (StackTraceElement traceElement : throwable.getStackTrace()) {
                error.append('\n');
                error.append("[File: ");
                error.append(traceElement.getFileName());
                error.append(", Line: ");
                error.append(traceElement.getLineNumber());
                error.append("] ");
                error.append(traceElement.getClassName());
                error.append(".");
                error.append(traceElement.getMethodName());
                error.append("([NATIVE])");
            }
        } else {
            error.append(throwable.getMessage());
        }

        return new RestError(error.toString(), RestStatus.INTERNAL_SERVER_ERROR);
    }
}
