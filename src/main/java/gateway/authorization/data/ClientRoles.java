package gateway.authorization.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


/**
 *
 * @author terryemailbox@gmail.com
 *
 */
@Entity
public class ClientRoles {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected long id;

    @Column(nullable = false)
    protected String clientId;

//    @Column(nullable = false)
//    private String serviceName;

    @Column(nullable = false)
    protected String role;

    @Column(nullable = false)
    protected String gatewayPolicyId;

//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "gatewayPolicyId", nullable = false)
//    private GatewayPolicy gatewayPolicy;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String userId) {
        this.clientId = userId;
    }

//    public String getServiceName() {
//        return serviceName;
//    }
//
//    public void setServiceName(String serviceName) {
//        this.serviceName = serviceName;
//    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getGatewayPolicyId() {
        return gatewayPolicyId;
    }

    public void setGatewayPolicyId(String gatewayPolicyId) {
        this.gatewayPolicyId = gatewayPolicyId;
    }

}
