package gateway.authorization.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import gateway.common.HttpMethod;

/**
 * @author terryemailbox@gmail.com
 */
public interface GatewayPolicyRepository extends JpaRepository<GatewayPolicy, Long> {
    List<GatewayPolicy> findByServiceNameAndHttpMethod(String serviceName, HttpMethod httpMethod);
}
