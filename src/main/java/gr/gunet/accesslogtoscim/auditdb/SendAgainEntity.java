package gr.gunet.accesslogtoscim.auditdb;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="SendAgain")
public class SendAgainEntity implements Serializable {
    @Id
    @Column(name="accesslogID",columnDefinition="int unsigned")
    private Integer accesslogID;
    
    @Id
    @Column(name="scimServer",columnDefinition="varchar(64)")
    private String scimServer;
    
    @Column(name="failedAttempts",columnDefinition="int unsigned")
    private Integer failedAttempts;

    public Integer getAccesslogID() {
        return accesslogID;
    }

    public void setAccesslogID(Integer accesslogID) {
        this.accesslogID = accesslogID;
    }

    public String getScimServer() {
        return scimServer;
    }

    public void setScimServer(String scimServer) {
        this.scimServer = scimServer;
    }

    public Integer getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(Integer failedAttempts) {
        this.failedAttempts = failedAttempts;
    }
}
