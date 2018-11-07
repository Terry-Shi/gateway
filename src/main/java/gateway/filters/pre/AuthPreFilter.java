package gateway.filters.pre;

import static gateway.filters.pre.FilterHelper.setErrorResponse;
import static gateway.filters.pre.FilterHelper.setRequestHeader;

import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import gateway.common.Constants;
import gateway.common.HttpMethod;
import gateway.common.error.Err;
import gateway.policy.PermissionManager;
import gateway.token.Token;
import gateway.token.TokenManager;

/**
 *
 * @author Terry
 *
 */
public class AuthPreFilter extends ZuulFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthPreFilter.class);

	@Inject
    private TokenManager tokenManager;

	@Inject
	private PermissionManager permissionManager;

	@Override
	public boolean shouldFilter() {
		return !permissionManager.allowAll;
	}

	@Override
	public Object run() {
		// 检查是否对请求的URL有权限访问
        checkPermission();
		return null;
	}

	@Override
	public String filterType() {
		return FilterConstants.PRE_TYPE;
	}

	@Override
	public int filterOrder() {
	    return FilterConstants.PRE_DECORATION_FILTER_ORDER + 10;
	}

    private String extractService(String requestUrl) {
        String serviceName = "";
        URL uri;
        try {
            uri = new URL(requestUrl);
            String path = uri.getPath();
            if (path.startsWith("/gw/")) {
                // "/gw/{service name}/..."
                serviceName = StringUtils.substringBetween(path, "/gw/", "/");
            }
        } catch (MalformedURLException e) {
            logger.error("get service name from URL: {}", requestUrl);
        }
        logger.debug("get service name from URL: {}", serviceName);
        return serviceName;
    }

	private boolean checkPermission() {
        HttpServletRequest req = RequestContext.getCurrentContext().getRequest();
        String requestUrl = req.getRequestURL().toString();
        String serviceName = extractService(requestUrl);
        HttpMethod httpMethod = HttpMethod.valueOf(req.getMethod());
        // 部分服务的URL允许匿名访问
        if (permissionManager.isAnonymousAllowed(serviceName, requestUrl, httpMethod)) {
            return true;
        }

        Token token = extractToken();
        String clientId = null;
        if (token != null && token.isValid()) {
            clientId = token.getUserId();
            logger.debug("The clientId was found in token: {}", clientId);
            setRequestHeader(Constants.USER_ID, clientId);

            // 超时
            if (token.getExpirationTime().getTime() < System.currentTimeMillis()) {
                logger.info("The token is expired: " + token.getExpirationTime());
                setErrorResponse(Err.TOKEN_EXPIRED);
                token.setValid(false);
                return false;
            }
        } else {
            logger.info("The token is empty or invalid");
            setErrorResponse(Err.USER_NOT_AUTHENTICATED);
            return false;
        }

        if (permissionManager.isClientAllowed(serviceName, requestUrl, httpMethod, clientId)) {
            return true;
        } else {
            logger.info("The client is not allowed to access url. clientId={}, url={}, httpMethod={}",
                clientId, requestUrl, httpMethod.toString());
            setErrorResponse(Err.USER_NOT_AUTHORIZED);
            return false;
        }
    }

    public Token extractToken() {
        HttpServletRequest req = RequestContext.getCurrentContext().getRequest();
        String authHeader = req.getHeader("authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring("Bearer ".length());
        return tokenManager.decodeToken(token);
    }

}
