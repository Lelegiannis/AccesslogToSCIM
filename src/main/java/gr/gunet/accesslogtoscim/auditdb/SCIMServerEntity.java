package gr.gunet.accesslogtoscim.auditdb;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="SCIMServer")
public class SCIMServerEntity implements Serializable {
    @Id
    @Column(name="acronym",columnDefinition="varchar(64)")
    private String acronym;
    
    @Column(name="fullName",columnDefinition="varchar(255)")
    private String fullName;
    
    @Column(name="scimVersion",columnDefinition="varchar(16)")
    private String scimVersion;
    
    @Column(name="authenticationMethod",columnDefinition="varchar(16)")
    private String authenticationMethod;

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getScimVersion() {
        return scimVersion;
    }

    public void setScimVersion(String scimVersion) {
        this.scimVersion = scimVersion;
    }

    public String getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(String authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }
}
