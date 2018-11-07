package gateway.common.error;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ErrResponse {
    private int code;
    private String message;
    private Map<String, String> payload = new HashMap<>();

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, String> payload) {
        this.payload = payload;
    }

    public static ErrResponse build(Err err) {
        return build(err, null);
    }

    public static ErrResponse build(Err err, Map<String, String> payload) {
        ErrResponse resp = new ErrResponse();
        resp.code = err.getCode();
        resp.message = err.getMessage();
        if (payload != null) {
            resp.payload.putAll(payload);
        }
        return resp;

    }

    @Override
    public String toString() {
        return "ErrResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", payload=" + payload +
                '}';
    }
}
