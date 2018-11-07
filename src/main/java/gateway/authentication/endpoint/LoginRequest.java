package gateway.authentication.endpoint;

import javax.validation.constraints.NotNull;

/**
 * @author Terry
 */
public class LoginRequest {

    @NotNull(message = "ClientId can not be null")
    private String clientId;

    @NotNull(message = "password can not be null")
    private String password;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String userId) {
        this.clientId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
