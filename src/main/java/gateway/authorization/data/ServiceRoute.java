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
public class ServiceRoute {


    /**
     * The ID of the route (the same as its map key by default).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    /**
     * The path (pattern) for the route, e.g. /foo/**.
     */
    @Column(nullable = false)
    private String path;

//    /**
//     * The service ID (if any) to map to this route. You can specify a physical URL or
//     * a service, but not both.
//     */
//    private String serviceId;

    @Column(nullable = false)
    private String serviceName;

    /**
     * A full physical URL to map to the route. An alternative is to use a service ID
     * and service discovery to find the physical address.
     */
    @Column(nullable = false)
    private String url;

    /**
     * Flag to determine whether the prefix for this route (the path, minus pattern
     * patcher) should be stripped before forwarding.
     */
    private boolean stripPrefix = true;

    /**
     * Flag to indicate that this route should be retryable (if supported). Generally
     * retry requires a service ID and ribbon.
     */
    private Boolean retryable;

    private Boolean enabled;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isStripPrefix() {
        return stripPrefix;
    }

    public void setStripPrefix(boolean stripPrefix) {
        this.stripPrefix = stripPrefix;
    }

    public Boolean getRetryable() {
        return retryable;
    }

    public void setRetryable(Boolean retryable) {
        this.retryable = retryable;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
