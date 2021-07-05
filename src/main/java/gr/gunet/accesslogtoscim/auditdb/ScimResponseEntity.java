package gr.gunet.accesslogtoscim.auditdb;

import java.io.Serializable;
import java.sql.Timestamp;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="SCIMResponse")
public class ScimResponseEntity implements Serializable {
    @Id
    @Column(name="responseID",columnDefinition="int unsigned")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long responseID;
    
    @Column(name="accesslogID",columnDefinition="int unsigned")
    private Integer accesslogID;
    
    @Column(name="requestMethod",columnDefinition="varchar(16)")
    private String requestMethod;
    
    @Column(name="requestType",columnDefinition="varchar(16)")
    private String requestType;
    
    @Column(name="receivingServer",columnDefinition="varchar(64)")
    private String receivingServer;
    
    @Column(name="body",columnDefinition="json")
    private String body;
    
    @Column(name="reqSentTimestamp",columnDefinition="timestamp")
    private Timestamp reqSentTimestamp;
    
    @Column(name="resReceivedTimestamp",columnDefinition="timestamp")
    private Timestamp resReceivedTimestamp;
    
    @Column(name="responseCode",columnDefinition="int unsigned")
    private Integer responseCode;
    
    @Column(name="errorType",columnDefinition="varchar(32)")
    private String errorType;
    
    @Column(name="errorDetails",columnDefinition="varchar(255)")
    private String errorDetails;
    
    @Column(name="attempt",columnDefinition="int unsigned")
    private Integer attempt;

    public Long getResponseID() {
        return responseID;
    }

    public void setResponseID(Long requestID) {
        this.responseID = requestID;
    }

    public Integer getAccesslogID() {
        return accesslogID;
    }

    public void setAccesslogID(Integer accesslogID) {
        this.accesslogID = accesslogID;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getReceivingServer() {
        return receivingServer;
    }

    public void setReceivingServer(String receivingServer) {
        this.receivingServer = receivingServer;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Timestamp getReqSentTimestamp() {
        return reqSentTimestamp;
    }

    public void setReqSentTimestamp(Timestamp reqSentTimestamp) {
        this.reqSentTimestamp = reqSentTimestamp;
    }

    public Timestamp getResReceivedTimestamp() {
        return resReceivedTimestamp;
    }

    public void setResReceivedTimestamp(Timestamp resReceivedTimestamp) {
        this.resReceivedTimestamp = resReceivedTimestamp;
    }

    public Integer getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(Integer responseCode) {
        this.responseCode = responseCode;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public Integer getAttempt() {
        return attempt;
    }

    public void setAttempt(Integer attempt) {
        this.attempt = attempt;
    }
}
