package gateway.authentication.endpoint;

import javax.validation.constraints.NotNull;

public class RefreshTokenRequest {
    @NotNull(message = "refresh token can not be null")
    private String refreshToken;

    @NotNull(message = "token can not be null")
    private String token;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
