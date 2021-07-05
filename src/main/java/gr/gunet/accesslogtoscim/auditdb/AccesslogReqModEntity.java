package gr.gunet.accesslogtoscim.auditdb;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="AccesslogReqModEntry")
public class AccesslogReqModEntity implements Serializable {
    @Id
    @Column(name="accesslogID",columnDefinition="int unsigned")
    private Integer accesslogID;
    
    @Id
    @Column(name="attributeName",columnDefinition="varchar(64)")
    private String attributeName;
    
    @Id
    @Column(name="value",columnDefinition="varchar(255)")
    private String value;
    
    @Column(name = "operation",columnDefinition="varchar(1)")
    private String operation;

    public Integer getAccesslogID() {
        return accesslogID;
    }

    public void setAccesslogID(Integer accesslogID) {
        this.accesslogID = accesslogID;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
