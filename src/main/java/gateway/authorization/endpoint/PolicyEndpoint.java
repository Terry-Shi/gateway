package gateway.authorization.endpoint;

import gateway.authorization.data.GatewayPolicy;
import gateway.authorization.data.GatewayPolicyRepository;
import gateway.common.HttpMethod;

import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 *
 */
@Component
@Path("policy")
@Produces(MediaType.APPLICATION_JSON)
public class PolicyEndpoint {
    @Inject
    GatewayPolicyRepository repository;

    @GET
    @Path("/serviceName/{serviceName}/httpMethod/{httpMethod}")
    public Response get(@PathParam("serviceName") String serviceName, @PathParam("httpMethod") HttpMethod httpMethod) {
        return Response.ok(repository.findByServiceNameAndHttpMethod(serviceName, httpMethod)).build();
    }

    @POST
    public Response upload(List<GatewayPolicy> policies) {
        if (policies != null && policies.size() > 0) {
            repository.save(policies);
        }
        return Response.ok().build();
    }

    @DELETE
    public Response delete(List<GatewayPolicy> policies) {
        if (policies != null && policies.size() > 0) {
            repository.deleteInBatch(policies);
        }
        return Response.ok().build();
    }
}
