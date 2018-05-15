package gateway.policy;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import gateway.policy.data.ClientRolesBean;
import gateway.policy.data.ClientRolesRepository;
import gateway.policy.data.GatewayPolicy;
import gateway.policy.data.GatewayPolicyRepository;
import gateway.policy.domain.HttpMethod;


/**
 *
 */
@Component
public class DbPolicyManager {
    private static final Logger logger = LoggerFactory.getLogger(DbPolicyManager.class);
    public static final String STAR = "*";
    public static final String TWO_STARS = "**";

    private PathMatcher pathMatcher = new AntPathMatcher();

    @Inject
    GatewayPolicyRepository gatewayPolicyRepo;

    @Inject
    ClientRolesRepository clientRolesRepo;

    public List<String> getClientRoles(String clientId, String serviceName, String requestUrl, HttpMethod httpMethod) {
        try {
            URL url = new URL(requestUrl);
            String path = url.getPath();

            List<Object[]> rawdata = clientRolesRepo.findAllDataByClientIdAndServiceName(clientId, serviceName, httpMethod);
            List<ClientRolesBean> policies =  new ArrayList<>();
            for (Object[] objects : rawdata) {
                ClientRolesBean bean = new ClientRolesBean(Long.valueOf(objects[0].toString()), objects[1].toString(),objects[2].toString(),objects[3].toString(), objects[4].toString(), HttpMethod.valueOf(objects[5].toString()), objects[6].toString());
                policies.add(bean);
            }

            List<String> roles = new ArrayList<>();

            if (policies != null) {
                policies.forEach(policy -> {
                    if (pathMatcher.match(policy.getUrl(), path)) {
                        logger.debug("{} match pattern {}", requestUrl, policy.getUrl());
                        roles.add(policy.getRole());
                    }
                });
            }
            return roles;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<String> getServiceRoles(String serviceName, String requestUrl, HttpMethod httpMethod) {
        try {
            URL url = new URL(requestUrl);
            String path = url.getPath();

            List<GatewayPolicy> policies = gatewayPolicyRepo.findByServiceNameAndHttpMethod(serviceName, httpMethod);
            List<String> roles = new ArrayList<>();
            if (policies != null) {
                policies.forEach(policy -> {
                    if (pathMatcher.match(policy.getUrl(), path)) {
                        logger.debug("{} match pattern {}", requestUrl, policy.getUrl());
                        roles.add(policy.getRole());
                    }
                });
            }
            return roles;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

}
