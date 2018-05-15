package gateway.policy.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Terry
 *
 */
public interface ClientRepository extends JpaRepository<Client, String> {

    List<Client> findByClientId(String clientId);

}
