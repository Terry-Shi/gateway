package gateway.policy;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import gateway.policy.data.ClientRolesBean;
import gateway.policy.data.ClientRolesRepository;
import gateway.policy.data.GatewayPolicy;
import gateway.policy.data.GatewayPolicyRepository;
import gateway.policy.domain.HttpMethod;

/**
 *
 */
@Component
public class CachePolicyManager {

    private static final Logger logger = LoggerFactory.getLogger(CachePolicyManager.class);

    private PathMatcher pathMatcher = new AntPathMatcher();

    @Inject
    private GatewayPolicyRepository gatewayPlicyRepo;

    @Inject
    private ClientRolesRepository clientRolesRepo;

    // 存放所有service对用需要的role
    private Map<ServiceRolesKey, List<String>> allServiceRoles = new ConcurrentHashMap<>();

    // 存放所有client具备的role
    private Map<ClientIdServiceRolesKey, List<String>> allClientRoles = new ConcurrentHashMap<>();

    // 缓存
    private LoadingCache<ServiceRolesKey, List<String>> cachedServiceRoles = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(5, TimeUnit.DAYS)
            .build(new CacheLoader<ServiceRolesKey, List<String>>() {
                @Override
                public List<String> load(ServiceRolesKey key) throws Exception {
                    return loadServiceRoles(key.serviceName, key.requestUrl, key.httpMethod);
                }
            });

    // 缓存
    private LoadingCache<ClientIdServiceRolesKey, List<String>> cachedClientRoles = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterAccess(5, TimeUnit.DAYS)
            .build(new CacheLoader<ClientIdServiceRolesKey, List<String>>() {
                @Override
                public List<String> load(ClientIdServiceRolesKey key) throws Exception {
                    return loadClientRoles(key.getClientId(), key.getServiceName(), key.getRequestUrl(), key.getHttpMethod());
                }
            });

    @PostConstruct
    public void initGatewayPolicy() {
        // service' roles
        List<GatewayPolicy> gatewayPolicy = gatewayPlicyRepo.findAll(); // serviceName, url, method ---> roles
        for (GatewayPolicy item : gatewayPolicy) {
            ServiceRolesKey key = new ServiceRolesKey(item.getServiceName(), item.getUrl(), item.getHttpMethod());
            List<String> roles = allServiceRoles.get(key);
            if (roles == null) {
                roles = new ArrayList<String>();
                allServiceRoles.put(key, roles);
            }
            roles.add(item.getRole());
        }
    }

    @PostConstruct
    public void initClientRoles() {
        // client's roles
        List<Object[]> rawdata = clientRolesRepo.findAllData(); // clientID, serviceName, url, method ---> roles
        List<ClientRolesBean> clientRoles = new ArrayList<>();
        for (Object[] objects : rawdata) {
            ClientRolesBean bean = new ClientRolesBean(Long.valueOf(objects[0].toString()), objects[1].toString(),objects[2].toString(),objects[3].toString(), objects[4].toString(), HttpMethod.valueOf(objects[5].toString()), objects[6].toString());
            clientRoles.add(bean);
        }

        for (ClientRolesBean item : clientRoles) {
            ClientIdServiceRolesKey key = new ClientIdServiceRolesKey(item.getClientId(), item.getServiceName(), item.getUrl(), item.getHttpMethod());
            List<String> roles = allClientRoles.get(key);
            if (roles == null) {
                roles = new ArrayList<String>();
                allClientRoles.put(key, roles);
            }
            roles.add(item.getRole());
        }
    }

    /**
     *
     * @param serviceName
     * @param requestUrl  并非直接等于匹配，参考{@link #loadServiceRoles}
     * @param httpMethod
     * @return
     */
    public List<String> getServiceRoles(String serviceName, String requestUrl, HttpMethod httpMethod) {
        return cachedServiceRoles.getUnchecked(new ServiceRolesKey(serviceName, requestUrl, httpMethod));
    }

    /**
     *
     * @param clientId
     * @param serviceName
     * @param requestUrl  并非直接等于匹配，参考{@link #loadClientRoles}
     * @param httpMethod
     * @return
     */
    public List<String> getClientRoles(String clientId, String serviceName, String requestUrl, HttpMethod httpMethod) {
        return cachedClientRoles.getUnchecked(new ClientIdServiceRolesKey(clientId, serviceName, requestUrl, httpMethod));
    }

    public List<String> loadServiceRoles(String serviceName, String requestUrl, HttpMethod httpMethod) {
        String path = extractPath(requestUrl);

        List<String> ret = new ArrayList<>();
        for (Map.Entry<ServiceRolesKey, List<String>> entry : allServiceRoles.entrySet()) {
            if (entry.getKey().getServiceName().equals(serviceName) && pathMatcher.match(entry.getKey().getRequestUrl(), path) && entry.getKey().getHttpMethod().equals(httpMethod) ) {
                ret.addAll(entry.getValue());
            }
        }
        return ret;
    }

    public List<String> loadClientRoles(String clientId, String serviceName, String requestUrl, HttpMethod httpMethod) {
        String path = extractPath(requestUrl);

        List<String> ret = new ArrayList<>();
        for (Map.Entry<ClientIdServiceRolesKey, List<String>> entry : allClientRoles.entrySet()) {
            if (entry.getKey().getClientId().equals(clientId) && entry.getKey().getServiceName().equals(serviceName) && pathMatcher.match(entry.getKey().getRequestUrl(), path) && entry.getKey().getHttpMethod().equals(httpMethod) ) {
                ret.addAll(entry.getValue());
            }
        }
        return ret;
    }

    private String extractPath(String requestUrl) {
        try {
            URL url = new URL(requestUrl);
            return url.getPath();
        } catch (MalformedURLException e) {
            return "";
        }
    }

    class ClientIdServiceRolesKey {
        private String clientId;
        private String serviceName;
        private String requestUrl; // 从数据库取出的是url pattern，查询时输入的是实际的requestUrl
        private HttpMethod httpMethod;

        public ClientIdServiceRolesKey(String clientId, String serviceName, String requestUrl, HttpMethod httpMethod) {
            this.clientId = clientId;
            this.serviceName = serviceName;
            this.requestUrl = requestUrl;
            this.httpMethod = httpMethod;
        }
        public String getClientId() {
            return clientId;
        }
        public void setClientId(String clientId) {
            this.clientId = clientId;
        }
        public String getServiceName() {
            return serviceName;
        }
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
        public String getRequestUrl() {
            return requestUrl;
        }
        public HttpMethod getHttpMethod() {
            return httpMethod;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ClientIdServiceRolesKey that = (ClientIdServiceRolesKey) o;

            if (serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null) {
                return false;
            }
            if (clientId != null ? !clientId.equals(that.clientId) : that.clientId != null) {
                return false;
            }
            if (requestUrl != null ? !requestUrl.equals(that.requestUrl) : that.requestUrl != null) {
                return false;
            }
            return httpMethod == that.httpMethod;
        }

        @Override
        public int hashCode() {
            int result = serviceName != null ? serviceName.hashCode() : 0;
            result = 31 * result + (clientId != null ? clientId.hashCode() : 0);
            result = 31 * result + (requestUrl != null ? requestUrl.hashCode() : 0);
            result = 31 * result + (httpMethod != null ? httpMethod.hashCode() : 0);
            return result;
        }
    }


    class ServiceRolesKey {
        private String serviceName;
        private String requestUrl; // 从数据库取出的是url pattern，查询时输入的是实际的requestUrl
        private HttpMethod httpMethod;

        public ServiceRolesKey(String serviceName, String requestUrl, HttpMethod httpMethod) {
            this.serviceName = serviceName;
            this.requestUrl = requestUrl;
            this.httpMethod = httpMethod;
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getRequestUrl() {
            return requestUrl;
        }

        public HttpMethod getHttpMethod() {
            return httpMethod;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ServiceRolesKey that = (ServiceRolesKey) o;

            if (serviceName != that.serviceName) {
                return false;
            }
            if (requestUrl != null ? !requestUrl.equals(that.requestUrl) : that.requestUrl != null) {
                return false;
            }
            return httpMethod == that.httpMethod;
        }

        @Override
        public int hashCode() {
            int result = serviceName != null ? serviceName.hashCode() : 0;
            result = 31 * result + (requestUrl != null ? requestUrl.hashCode() : 0);
            result = 31 * result + (httpMethod != null ? httpMethod.hashCode() : 0);
            return result;
        }
    }
}
