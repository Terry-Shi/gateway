package gateway.policy.data;

import gateway.policy.domain.HttpMethod;

public class ClientRolesBean extends ClientRoles {

    private String serviceName;

    private HttpMethod httpMethod;

    private String url;

    public ClientRolesBean(long id, String clientId, String role, String gatewayPolicyId, String serviceName, HttpMethod httpMethod, String url) {
        super();
        this.id = id;
        this.clientId = clientId;
        this.role = role;
        this.gatewayPolicyId = gatewayPolicyId;
        this.serviceName = serviceName;
        this.httpMethod = httpMethod;
        this.url = url;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
