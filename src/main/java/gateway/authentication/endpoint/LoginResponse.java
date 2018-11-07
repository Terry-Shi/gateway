package gateway.authentication.endpoint;

import javax.ws.rs.core.Response;

import gateway.common.BaseResponse;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;

/**
 *
 */
public class LoginResponse extends BaseResponse {
    private String token;

    private LoginResponse(Builder builder) {
        super(builder.statusCode, builder.message);
        this.token = builder.token;
    }

    public String getToken() {
        return token;
    }

    public static class Builder {
        private static Map<Integer, String> messages = of(200, "You have logged in successfully", 401, "The username or password is incorrect");
        private int statusCode;
        private String token;
        private String message;

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode == 201 ? 200 : statusCode;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public LoginResponse build() {
            if (message == null)
                this.message = messages.getOrDefault(statusCode, Response.Status.fromStatusCode(statusCode).toString());
            return new LoginResponse(this);
        }
    }

    public Response asResponse() {
        return Response.status(this.status).entity(this).build();
    }

}
