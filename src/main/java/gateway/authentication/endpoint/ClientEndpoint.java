package gateway.authentication.endpoint;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gateway.authentication.data.Client;
import gateway.authentication.data.ClientRepository;
import gateway.authentication.util.SimpleSaltHash;
import gateway.common.BaseResponse;
import gateway.common.error.AppException;
import gateway.token.Token;
import gateway.token.TokenManager;


@Component
@Path("/client")
@Produces(MediaType.APPLICATION_JSON)
public class ClientEndpoint {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientEndpoint.class);

    @Value("${security.token-expire-time}")
    public long tokenExpireTime;

    @Inject
    private ClientRepository userRepository;

    @Inject
    private TokenManager tokenManager;

    @Path("/login") //http://localhost:8809/api/v1/client/login
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(@Valid LoginRequest request) {

        List<Client> result = userRepository.findByClientId(request.getClientId());
        if (result.size() == 0 ) {
            throw new AppException("Login failed, couldn't find Client id:" + request.getClientId());
        } else {
            String psw = result.get(0).getPassword();
            if (SimpleSaltHash.getMd5Hash(psw, SimpleSaltHash.salt).equals(request.getPassword())) { // 这里简化了密码的处理，正式项目可用salt+hash值存放密码
                // token 的有效时间可以配置
                Instant expiredTime = Instant.now().plus(tokenExpireTime, ChronoUnit.MINUTES);
                Instant refreshTokenExpiredTime = Instant.now().plus(tokenExpireTime*2, ChronoUnit.MINUTES);
                String token = tokenManager.generateToken(request.getClientId(), Date.from(expiredTime));
                String refreshToken = tokenManager.generateToken(request.getClientId(), Date.from(refreshTokenExpiredTime));
                LoginResponse loginResponse = new LoginResponse.Builder().statusCode(200).token(token).refreshToken(refreshToken).build();
                return loginResponse.asResponse();
            } else {
                throw new AppException("Login failed, password is not correct for : "+request.getClientId());
            }
        }
    }

    @Path("/refreshtoken") //http://localhost:8809/api/v1/client/refreshtoken
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response refreshToken(@Valid RefreshTokenRequest request) {

        // check if refresh token is valid
        Token decodedToken = tokenManager.decodeToken(request.getRefreshToken());
        if (decodedToken != null && decodedToken.isValid()) {
            // 超时
            if (decodedToken.getExpirationTime().getTime() < System.currentTimeMillis()) {
                logger.info("refresh token failed, The refreshToken is expired: " + decodedToken.getExpirationTime());
                throw new AppException("refresh token failed, refreshtoken is not valid.");
            }
            String clientId = decodedToken.getUserId();
            // token 的有效时间可以配置
            Instant expiredTime = Instant.now().plus(tokenExpireTime, ChronoUnit.MINUTES);
            Instant refreshTokenExpiredTime = Instant.now().plus(tokenExpireTime*2, ChronoUnit.MINUTES);
            String token = tokenManager.generateToken(clientId, Date.from(expiredTime));
            String refreshToken = tokenManager.generateToken(clientId, Date.from(refreshTokenExpiredTime));
            LoginResponse loginResponse = new LoginResponse.Builder().statusCode(200).token(token).refreshToken(refreshToken).build();
            return loginResponse.asResponse();
        } else {
            logger.info("refresh token failed, refreshtoken is not valid.");
            throw new AppException("refresh token failed, refreshtoken is not valid.");
        }
       
    }
    
    @Path("/add")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response add(@Valid Client request) {
        try {
            List<Client> clients = userRepository.findByClientId(request.getClientId());
            if (clients.size() == 0) {
                request.setPassword(SimpleSaltHash.getMd5Hash(request.getPassword(), SimpleSaltHash.salt));
                userRepository.save(request);
            } else {
                throw new Exception("User already exists!");
            }
            BaseResponse response = new BaseResponse(200, "client created");
            return response.asResponse();
        } catch (Exception e) {
            throw new AppException("add client failed: " + e.getMessage(), e);
        }
    }

    @Path("/delete/{id}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        try {
            userRepository.delete(id);
            BaseResponse response = new BaseResponse(200, "client deleted");
            return response.asResponse();
        } catch (Exception e) {
            throw new AppException("delete client failed: " + e.getMessage());
        }
    }

    @Path("/list")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public List<Client> list() {
        try {
            List<Client> clients = userRepository.findAll();
            return clients;
        } catch (Exception e) {
            throw new AppException("list client failed: " + e.getMessage());
        }
    }

}
