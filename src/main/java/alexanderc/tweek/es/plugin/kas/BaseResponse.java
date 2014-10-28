package alexanderc.tweek.es.plugin.kas;

import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.rest.*;

/**
 * Created by AlexanderC on 10/28/14.
 */
abstract public class BaseResponse extends RestResponse {

    private RestStatus status = RestStatus.OK;

    private BytesReference bytes;

    @Override
    public final String contentType() {
        return "json";
    }

    @Override
    public final boolean contentThreadSafe() {
        return true;
    }

    @Override
    public final BytesReference content() {
        return bytes;
    }

    @Override
    public final RestStatus status() {
        return status;
    }

    public final BaseResponse setStatus(final RestStatus status) {
        this.status = status;

        return this;
    }

    public final RestStatus getStatus() {
        return status;
    }

    public final BytesReference getBytes() {
        return bytes;
    }

    public final BaseResponse setBytes(BytesReference bytes) {
        this.bytes = bytes;

        return this;
    }
}