package gateway.endpoint;

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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gateway.error.AppException;
import gateway.policy.data.Client;
import gateway.policy.data.ClientRepository;
import gateway.token.TokenManager;


@Component
@Path("/client")
@Produces(MediaType.APPLICATION_JSON)
public class ClientEndpoint {

    @Value("${security.token-expire-hours}")
    public long tokenExpireHurs;

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
            throw new AppException("Login failed, couldn't found Client id:" + request.getClientId());
        } else {
            String psw = result.get(0).getPassword();
            if (psw.equals(request.getPassword())) { // 原型中简化了密码的处理，正式项目可用hash值存放密码
                // token 的有效时间可以配置
                Instant expiredTime = Instant.now().plus(tokenExpireHurs, ChronoUnit.HOURS);

                String token = tokenManager.generateToken(request.getClientId(), Date.from(expiredTime));
                LoginResponse loginResponse = new LoginResponse.Builder().statusCode(200).token(token).build();
                return loginResponse.asResponse();
            } else {
                throw new AppException("Login failed, password is not correct for : "+request.getClientId());
            }
        }
    }

    @Path("/add")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response add(@Valid Client request) {
        try {
            userRepository.save(request);
            BaseResponse response = new BaseResponse(200, "client created");
            return response.asResponse();
        } catch (Exception e) {
            throw new AppException("add client failed: " + e.getMessage());
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
