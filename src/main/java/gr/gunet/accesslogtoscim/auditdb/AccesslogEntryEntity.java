package gr.gunet.accesslogtoscim.auditdb;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="AccesslogEntry")
public class AccesslogEntryEntity implements Serializable {
    @Id
    @Column(name="accesslogID",columnDefinition="int unsigned")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer accesslogID;
    
    @Column(name="UUID",columnDefinition="varchar(64)")
    private String uuid;
    
    @Column(name="reqType",columnDefinition="varchar(16)")
    private String reqType;
    
    @Column(name="reqDN",columnDefinition="varchar(255)")
    private String reqDN;
    
    @Column(name="reqStartTime",columnDefinition="int unsigned")
    private Long reqStartTime;
    
    @Column(name="reqStartCount",columnDefinition="int unsigned")
    private Integer reqStartCount;
    
    @Column(name="reqEndTime",columnDefinition="int unsigned")
    private Long reqEndTime;
    
    @Column(name="reqEndCount",columnDefinition="int unsigned")
    private Integer reqEndCount;

    public Integer getAccesslogID() {
        return accesslogID;
    }

    public void setAccesslogID(Integer accesslogID) {
        this.accesslogID = accesslogID;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getReqType() {
        return reqType;
    }

    public void setReqType(String reqType) {
        this.reqType = reqType;
    }

    public String getReqDN() {
        return reqDN;
    }

    public void setReqDN(String reqDN) {
        this.reqDN = reqDN;
    }

    public Long getReqStartTime() {
        return reqStartTime;
    }

    public void setReqStartTime(Long reqStartTime) {
        this.reqStartTime = reqStartTime;
    }

    public Integer getReqStartCount() {
        return reqStartCount;
    }

    public void setReqStartCount(Integer reqStartCount) {
        this.reqStartCount = reqStartCount;
    }

    public Long getReqEndTime() {
        return reqEndTime;
    }

    public void setReqEndTime(Long reqEndTime) {
        this.reqEndTime = reqEndTime;
    }

    public Integer getReqEndCount() {
        return reqEndCount;
    }

    public void setReqEndCount(Integer reqEndCount) {
        this.reqEndCount = reqEndCount;
    }
}
