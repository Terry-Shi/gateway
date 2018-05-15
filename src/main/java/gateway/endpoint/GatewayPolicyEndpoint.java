package gateway.endpoint;

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

import org.springframework.stereotype.Component;

import gateway.error.AppException;
import gateway.policy.CachePolicyManager;
import gateway.policy.PermissionManager;
import gateway.policy.data.GatewayPolicy;
import gateway.policy.data.GatewayPolicyRepository;

@Component
@Path("/gatewaypolicy")
@Produces(MediaType.APPLICATION_JSON)
public class GatewayPolicyEndpoint {
    @Inject
    PermissionManager permissionManager;
    @Inject
    CachePolicyManager cachePolicyManager;
    @Inject
    GatewayPolicyRepository repo;

    @Path("/add")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response add(@Valid GatewayPolicy request) {
        try {
            repo.save(request);
            BaseResponse response = new BaseResponse(200, "Gateway Policy created");
            if (permissionManager.cacheEnabled) {
                cachePolicyManager.initGatewayPolicy();
            }
            return response.asResponse();
        } catch (Exception e) {
            throw new AppException("Add Gateway Policy failed: " + e.getMessage());
        }
    }

    @Path("/delete/{id}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        try {
            repo.delete(Long.valueOf(id));
            BaseResponse response = new BaseResponse(200, "Gateway Policy deleted");
            if (permissionManager.cacheEnabled) {
                cachePolicyManager.initGatewayPolicy();
            }
            return response.asResponse();
        } catch (Exception e) {
            throw new AppException("delete Gateway Policy failed: " + e.getMessage());
        }
    }

    @Path("/list")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public List<GatewayPolicy> list() {
        try {
            List<GatewayPolicy> clients = repo.findAll();
            return clients;
        } catch (Exception e) {
            throw new AppException("list Gateway Policy failed: " + e.getMessage());
        }
    }

}
