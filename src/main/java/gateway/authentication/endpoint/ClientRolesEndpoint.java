package gateway.authentication.endpoint;

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

import gateway.authorization.data.ClientRoles;
import gateway.authorization.data.ClientRolesRepository;
import gateway.common.BaseResponse;
import gateway.common.error.AppException;
import gateway.policy.CachePolicyManager;
import gateway.policy.PermissionManager;

@Component
@Path("/clientroles")
@Produces(MediaType.APPLICATION_JSON)
public class ClientRolesEndpoint {
    @Inject
    PermissionManager permissionManager;
    @Inject
    CachePolicyManager cachePolicyManager;
    @Inject
    ClientRolesRepository repo;

    @Path("/add")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response add(@Valid ClientRoles request) {
        try {
            repo.save(request);
            BaseResponse response = new BaseResponse(200, "Client's Role created");
            if (permissionManager.cacheEnabled) {
                cachePolicyManager.initClientRoles();
            }
            return response.asResponse();
        } catch (Exception e) {
            throw new AppException("Add client role failed: " + e.getMessage());
        }
    }

    @Path("/delete/{id}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        try {
            repo.delete(Long.valueOf(id));
            BaseResponse response = new BaseResponse(200, "client deleted");
            if (permissionManager.cacheEnabled) {
                cachePolicyManager.initClientRoles();
            }
            return response.asResponse();
        } catch (Exception e) {
            throw new AppException("delete client role failed: " + e.getMessage());
        }
    }

    @Path("/list")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public List<ClientRoles> list() {
        try {
            List<ClientRoles> clients = repo.findAll();
            return clients;
        } catch (Exception e) {
            throw new AppException("list client's roles failed: " + e.getMessage());
        }
    }
}
