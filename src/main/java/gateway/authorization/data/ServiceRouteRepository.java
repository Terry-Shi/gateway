package gateway.authorization.data;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author terryemailbox@gmail.com
 *
 */
public interface ServiceRouteRepository extends JpaRepository<ServiceRoute, Long> {
    List<ServiceRoute> findByEnabled(Boolean enabled);
}
