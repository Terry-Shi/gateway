package gateway.common.error;

import org.springframework.core.NestedRuntimeException;

/**
 *
 */
public class AppException extends NestedRuntimeException {

    private static final long serialVersionUID = 7973935268244415641L;

    public AppException(String msg) {
        super(msg);
    }

    public AppException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
