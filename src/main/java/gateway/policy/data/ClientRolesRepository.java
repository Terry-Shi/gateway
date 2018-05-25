package gateway.policy.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import gateway.policy.domain.HttpMethod;

/**
 *
 * @author Terry
 *
 */
public interface ClientRolesRepository extends JpaRepository<ClientRoles, Long> {

    public List<ClientRoles> findByClientId(String clientId);

    public void deleteByClientIdAndGatewayPolicyId(String clientId, String gatewayPolicyId);

    @Query(value = "select e.id, e.client_id, e.role, e.gateway_policy_id, d.service_name, d.http_method, d.url "
        + " from client_roles e "
        + " left join gateway_policy d on d.id = e.gateway_policy_id "
        ,
        nativeQuery = true)
    public List<Object[]> findAllData();

    @Query(value = "select e.id, e.client_id, e.role, e.gateway_policy_id, d.service_name, d.http_method, d.url "
            + " from client_roles e "
            + " left join gateway_policy d on d.id = e.gateway_policy_id "
            + "where e.client_id = ?1 "
            + "  and d.service_name = ?2 "
            + "  and d.http_method = ?3 "
            ,
            nativeQuery = true)
    public List<Object[]> findAllDataByClientIdAndServiceName(String clientId, String serviceName, String httpMethod);


}
