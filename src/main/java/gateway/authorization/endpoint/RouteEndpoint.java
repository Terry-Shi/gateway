package gateway.authorization.endpoint;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.web.ZuulController;
import org.springframework.cloud.netflix.zuul.web.ZuulHandlerMapping;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import gateway.RefreshRouteService;

/**
 *
 * @author Terry
 *
 */
@Component
@Path("/route")
@Produces(MediaType.APPLICATION_JSON)
public class RouteEndpoint {

    @Autowired
    RefreshRouteService refreshRouteService;

    @RequestMapping("/refreshroute")
    public String refreshRoute(){
        refreshRouteService.refreshRoute();
        return "refreshRoute";
    }

    @Autowired
    ZuulHandlerMapping zuulHandlerMapping;

    @RequestMapping("/watchroute")
    public Map<String, Object> watchRoute(){
        //可以用debug模式看里面具体是什么
        Map<String, Object> handlerMap = zuulHandlerMapping.getHandlerMap();

        Map<String, Object> ret = new LinkedHashMap<>();
        for  (String key : handlerMap.keySet()) {
            ret.put(key, ((ZuulController)handlerMap.get(key)).toString());
            System.out.println(key + " = " + ((ZuulController)handlerMap.get(key)).toString() );
        }
        return ret;
    }

}
