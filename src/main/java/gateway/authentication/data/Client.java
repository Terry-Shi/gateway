package gateway.authentication.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


/**
 *
 * @author terryemailbox@gmail.com
 *
 */
@Entity
public class Client {
    @Id
    @Column(nullable = false)
    private String clientId;

    @Column(nullable = false)
    private String password;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String userId) {
        this.clientId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}
