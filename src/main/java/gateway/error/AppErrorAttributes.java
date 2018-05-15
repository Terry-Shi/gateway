package gateway.error;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;

import com.google.common.collect.ImmutableMap;

/**
 *
 */
@Component
public class AppErrorAttributes extends DefaultErrorAttributes {
    @Override
    public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes, boolean includeStackTrace) {
        Err err = Err.SERVER_ERROR;

        Map<String, Object> payload = new HashMap<>();
        Throwable throwable = getError(requestAttributes);
        if (throwable != null) {
            Throwable cause = throwable.getCause();
            if (cause != null) {
                payload.put("root_cause", cause.getMessage());
            }
        }

        return ImmutableMap.<String, Object>builder()
                .put("code", err.getCode())
                .put("message", err.getMessage())
                .put("payload", payload)
                .build();
    }
}
