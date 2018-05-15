package gateway.policy;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gateway.policy.domain.HttpMethod;

import javax.inject.Inject;
import java.util.List;


/**
 * @author wangchunyang@gmail.com
 */
@Component
public class PermissionManager {
    private static final Logger logger = LoggerFactory.getLogger(PermissionManager.class);

    @Value("${security.allowAll}")
    public boolean allowAll;

    @Value("${security.cache-enabled}")
    public boolean cacheEnabled;

    @Inject
    private DbPolicyManager dbPolicyManager;

    @Inject
    private CachePolicyManager cachePolicyManager;

    public final static String ROLE_TYPE_SYS_ADMIN = "SYS_ADMIN";
    public final static String ROLE_TYPE_ANONYMOUS = "ANONYMOUS";
//    public final static String ROLE_TYPE_FORBIDDEN = "FORBIDDEN";

    public boolean isAnonymousAllowed(String serviceName, String url, HttpMethod httpMethod) {
        List<String> roles = cacheEnabled ? cachePolicyManager.getServiceRoles(serviceName, url, httpMethod) : dbPolicyManager.getServiceRoles(serviceName, url, httpMethod);
        boolean result = roles.contains(ROLE_TYPE_ANONYMOUS);
        logger.debug("Allow anonymous access for {} -> {}", url, result);
        return result;
    }

    public boolean isClientAllowed(String serviceName, String requestUrl, HttpMethod httpMethod, String clientId) {
        List<String> roles = cacheEnabled ? cachePolicyManager.getServiceRoles(serviceName, requestUrl, httpMethod) : dbPolicyManager.getServiceRoles(serviceName, requestUrl, httpMethod);
        if (roles.size() == 0) {
            logger.debug("The allowed roles are not found for url {}. The access will be denied.", requestUrl);
            return false;
        }

        List<String> roleAssignments = cacheEnabled ? cachePolicyManager.getClientRoles(clientId, serviceName, requestUrl, httpMethod) : dbPolicyManager.getClientRoles(clientId, serviceName, requestUrl, httpMethod);
        for (String role : roles) {
            if (matchRole(role, roleAssignments)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchRole(String allowedRole, List<String> roleAssignments) {
        if (hasRole(roleAssignments, ROLE_TYPE_SYS_ADMIN)) {
            return true;
        }

        // check for other roles
       logger.debug("allowedRole is " + allowedRole + "; role assigned are: " + roleAssignments);
       return hasRole(roleAssignments, allowedRole);
    }

    private boolean hasRole(List<String> roleAssignments, String allowedRole) {
        for (String assignment : roleAssignments) {
            if (assignment.equals(allowedRole)) {
                return true;
            }
        }
        return false;
    }
}
