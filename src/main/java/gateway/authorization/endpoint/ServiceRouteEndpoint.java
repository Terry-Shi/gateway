package gateway.authorization.endpoint;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.RoutesRefreshedEvent;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import gateway.authorization.data.ServiceRoute;
import gateway.authorization.data.ServiceRouteRepository;
import gateway.common.BaseResponse;
import gateway.common.error.AppException;

@Component
@Path("/serviceroute")
@Produces(MediaType.APPLICATION_JSON)
public class ServiceRouteEndpoint {
    @Autowired
    ApplicationEventPublisher publisher;
    @Autowired
    RouteLocator routeLocator;
    @Inject
    ServiceRouteRepository repo;

    @Path("/add")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response add(@Valid ServiceRoute request) {
        try {
            repo.save(request);
            BaseResponse response = new BaseResponse(200, "Service Route created");
            refreshRoute();
            return response.asResponse();
        } catch (Exception e) {
            throw new AppException("Add Service Route failed: " + e.getMessage());
        }
    }

    @Path("/delete/{id}")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") String id) {
        try {
            repo.delete(Long.valueOf(id));
            BaseResponse response = new BaseResponse(200, "Service Route deleted");
            refreshRoute();
            return response.asResponse();
        } catch (Exception e) {
            throw new AppException("delete Service Route failed: " + e.getMessage());
        }
    }

    @Path("/list")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    public List<ServiceRoute> list() {
        try {
            List<ServiceRoute> clients = repo.findAll();
            return clients;
        } catch (Exception e) {
            throw new AppException("list Service Route failed: " + e.getMessage());
        }
    }

    public void refreshRoute() {
        RoutesRefreshedEvent routesRefreshedEvent = new RoutesRefreshedEvent(routeLocator);
        publisher.publishEvent(routesRefreshedEvent);
    }
}
