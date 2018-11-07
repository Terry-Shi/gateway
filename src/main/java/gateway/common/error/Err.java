package gateway.common.error;

/**
 *
 */
public enum Err {
    USER_NOT_AUTHENTICATED(8090001, "the user's token is invalid", 401),
    USER_NOT_AUTHORIZED(8090002, "the user has no permission to access the resources", 403),
    TOKEN_EXPIRED(8090003, "the token is expired", 401),
    SERVER_ERROR(4, "Internal server error", 500);

    private final int code;
    private final String message;
    private final int httpStatus;

    Err(int code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String toString() {
        return "Err{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", httpStatus=" + httpStatus +
                '}';
    }
}
