package gateway;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.RefreshableRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.SimpleRouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.util.StringUtils;

import gateway.authorization.data.ServiceRoute;
import gateway.authorization.data.ServiceRouteRepository;

/**
 *
 * @author Terry
 *
 */
public class CustomRouteLocator extends SimpleRouteLocator implements RefreshableRouteLocator{

    public final static Logger logger = LoggerFactory.getLogger(CustomRouteLocator.class);

    private ZuulProperties properties;

    private ServiceRouteRepository serviceRouteRepository;

    public void setRepo(ServiceRouteRepository serviceRouteRepository){
        this.serviceRouteRepository = serviceRouteRepository;
    }

    public CustomRouteLocator(String servletPath, ZuulProperties properties) {
        super(servletPath, properties);
        this.properties = properties;
        logger.info("servletPath:{}",servletPath);
    }

    @Override
    public void refresh() {
        doRefresh();
    }

    @Override
    protected Map<String, ZuulRoute> locateRoutes() {
        LinkedHashMap<String, ZuulRoute> routesMap = new LinkedHashMap<String, ZuulRoute>();
        //从application.properties中加载路由信息
        routesMap.putAll(super.locateRoutes());
        System.out.println("从application.properties中加载路由信息 = " + routesMap);
        //从db中加载路由信息
        routesMap.putAll(locateRoutesFromDB());

        //优化一下配置
        LinkedHashMap<String, ZuulRoute> values = new LinkedHashMap<>();
        for (Map.Entry<String, ZuulRoute> entry : routesMap.entrySet()) {
            String path = entry.getKey();
            // Prepend with slash if not already present.
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            if (StringUtils.hasText(this.properties.getPrefix())) {
                path = this.properties.getPrefix() + path;
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
            }
            values.put(path, entry.getValue());
        }
        System.out.println("结果 locateRoutes=" + values);
        return values;
    }

    private Map<String, ZuulRoute> locateRoutesFromDB(){
        Map<String, ZuulRoute> routes = new LinkedHashMap<>();
        List<ServiceRoute> results = serviceRouteRepository.findAll();
        for (ServiceRoute result : results) {
            if(org.apache.commons.lang3.StringUtils.isBlank(result.getPath()) || org.apache.commons.lang3.StringUtils.isBlank(result.getUrl()) ){
                continue;
            }
            ZuulRoute zuulRoute = new ZuulRoute();
            try {
                //org.springframework.beans.BeanUtils.copyProperties(result,zuulRoute);
                zuulRoute.setId(result.getServiceName());
                zuulRoute.setPath(result.getPath());
                zuulRoute.setUrl(result.getUrl());
                zuulRoute.setRetryable(result.getRetryable());
                zuulRoute.setStripPrefix(result.getRetryable());
                zuulRoute.setCustomSensitiveHeaders(true);
                zuulRoute.setSensitiveHeaders( new HashSet<String>());
                // don't set serviceId here

            } catch (Exception e) {
                logger.error("=============load zuul route info from db with error==============",e);
            }
            routes.put(zuulRoute.getPath(),zuulRoute);
        }
        return routes;
    }
}
