package gateway.endpoint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.ws.rs.core.Response;

/**
 *
 */
public class BaseResponse {

    @JsonProperty("status")
    protected int statusCode;

    @JsonIgnore
    protected Response.Status status;

    protected String message;

    public BaseResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
        this.status = Response.Status.fromStatusCode(statusCode);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Response.Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    @JsonIgnore
    public boolean isSuccessful() {
        return status.getStatusCode() >= 200 && status.getStatusCode() < 300;
    }

    public Response asResponse() {
        return Response.status(this.status).entity(this).build();
    }
}
