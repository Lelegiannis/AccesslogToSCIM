package gr.gunet.accesslogtoscim.idserver;

import gr.gunet.accesslogtoscim.auditdb.AuditDB;
import gr.gunet.accesslogtoscim.auditdb.DBManager;
import gr.gunet.accesslogtoscim.auditdb.SCIMServerEntity;
import gr.gunet.accesslogtoscim.auditdb.ScimResponseEntity;
import gr.gunet.accesslogtoscim.auditdb.SendAgainEntity;
import gr.gunet.accesslogtoscim.tools.PropertyReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import gr.gunet.accesslogtoscim.resource.ScimServerResource;

public abstract class ScimServer {
    private static final HashMap<String,ScimServer> INSTANCES = new HashMap();
    
    public static ScimServer getInstance(String identityServerAcronym) throws Exception{
        if(INSTANCES.containsKey(identityServerAcronym)){
            return INSTANCES.get(identityServerAcronym);
        }
        ScimServer scimManager;
        PropertyReader serverProps = new PropertyReader(identityServerAcronym+".properties");
        String scimVersion = serverProps.getProperty("scimVersion");
        if(scimVersion == null){
            throw new Exception("Identity Server '"+identityServerAcronym+"' has no 'scimVersion' property defined");
        }else if(scimVersion.equals("1.1")){
            scimManager = new Scim1_1Server(identityServerAcronym);
        }else if(scimVersion.equals("2")){
            scimManager = new Scim2Server(identityServerAcronym);
        }else{
            throw new Exception("Identity Server '"+identityServerAcronym+"' requires unsupported scimVersion '"+scimVersion+"'");
        }
        INSTANCES.put(identityServerAcronym, scimManager);
        scimManager.persist();
        return scimManager;
    }
    
    protected final String acronym;
    protected final String serverEndpoint;
    protected final String authMethod;
    protected final String username;
    protected final String password;
    protected final String token;
    
    private final AccesslogToScimMapping mapping;
    
    protected ScimResponseEntity lastResponse;
    
    public ScimServer(String identityServerAcronym) throws Exception{
        acronym = identityServerAcronym;
        PropertyReader serverProps = new PropertyReader(identityServerAcronym+".properties");
        
        serverEndpoint = serverProps.getProperty("endpoint");
        if(serverEndpoint == null){
            throw new Exception("Identity Server '"+acronym+"' has no 'endpoint' property defined");
        }
        
        authMethod = serverProps.getProperty("authenticationMethod");
        if(authMethod == null){
            throw new Exception("Identity Server '"+identityServerAcronym+"' has no 'authenticationMethod' property defined");
        }else if(authMethod.equals("token")){
            token = serverProps.getProperty("token");
            if(token == null){
                throw new Exception("Identity Server '"+identityServerAcronym+"' has token Authentication Mehtod, but 'token' property is not defined");
            }
            username = null;
            password = null;
        }else if(authMethod.equals("password")){
            username = serverProps.getProperty("username");
            if(username == null){
                throw new Exception("Identity Server '"+identityServerAcronym+"' has password Authentication Method, but 'username' property is not defined");
            }

            password = serverProps.getProperty("password");
            if(password == null){
                throw new Exception("Identity Server '"+identityServerAcronym+"' has password Authentication Method, but 'password' property is not defined");
            }
            token = null;
        }else{
            throw new Exception("Unknown authentication method '"+authMethod+"' on server '"+acronym+"'");
        }
        
        try{
            mapping = new AccesslogToScimMapping(identityServerAcronym+".mapping");
        }catch(Exception e){
            e.printStackTrace(System.err);
            throw new Exception("Could not open mapping file for identity server '"+identityServerAcronym+"'");
        }
        
        lastResponse = null;
    }
    
    public void sendNewRequest(AccesslogEntry accesslogEntry) throws Exception{
        accesslogEntry.persist();
        if(accesslogEntry.getReqType().equals("add")){
            ScimServerResource resource = generateEmptyResource();
            resource.addAttribute("externalId", accesslogEntry.getUUID());
            for(String attribute : mapping.getScimAttributes()){
                resource.addAttribute(attribute, mapping.resolveFromAccesslog(attribute, accesslogEntry));
            }

            createUser(resource);
            lastResponse.setAccesslogID(accesslogEntry.getAccesslogID());
            lastResponse.setAttempt(1);
            AuditDB.getInstance().insert(lastResponse);
            if(lastResponse.getResponseCode() != 200){
                SendAgainEntity sa = new SendAgainEntity();
                sa.setAccesslogID(accesslogEntry.getAccesslogID());
                sa.setFailedAttempts(1);
                sa.setScimServer(acronym);
                AuditDB.getInstance().insert(sa);
            }
        }else if(accesslogEntry.getReqType().equals("delete")){
            ScimServerResource userToDelete = fetchUserWithExternalID(accesslogEntry.getUUID());
            lastResponse.setAccesslogID(accesslogEntry.getAccesslogID());
            lastResponse.setAttempt(1);
            AuditDB.getInstance().insert(lastResponse);
            if(userToDelete != null){
                deleteUser(userToDelete);
                lastResponse.setAccesslogID(accesslogEntry.getAccesslogID());
                lastResponse.setAttempt(1);
                AuditDB.getInstance().insert(lastResponse);
                if(lastResponse.getResponseCode() == 200){
                    List<SendAgainEntity> obsoleteSendAgains = AuditDB.getInstance().select("SELECT sa FROM SendAgainEntity sa,AccesslogEntryEntity ae WHERE ae.accesslogID=sa.accesslogID AND (ae.reqEndTime < "+accesslogEntry.getReqStartTime()+" OR (ae.reqEndTime = "+accesslogEntry.getReqStartTime()+" AND ae.reqEndCount > "+accesslogEntry.getReqStartCount()+"))", SendAgainEntity.class);
                    for(SendAgainEntity sa : obsoleteSendAgains){
                        AuditDB.getInstance().delete(sa);
                    }
                }else{
                    SendAgainEntity sa = new SendAgainEntity();
                    sa.setAccesslogID(accesslogEntry.getAccesslogID());
                    sa.setFailedAttempts(1);
                    sa.setScimServer(acronym);
                    AuditDB.getInstance().insert(sa);
                }
            }else{
                SendAgainEntity sa = new SendAgainEntity();
                sa.setAccesslogID(accesslogEntry.getAccesslogID());
                sa.setFailedAttempts(1);
                sa.setScimServer(acronym);
                AuditDB.getInstance().insert(sa);
            }
        }else if(accesslogEntry.getReqType().equals("modify")){
            ScimServerResource userToModify = fetchUserWithExternalID(accesslogEntry.getUUID());
            lastResponse.setAccesslogID(accesslogEntry.getAccesslogID());
            lastResponse.setAttempt(1);
            AuditDB.getInstance().insert(lastResponse);
            if(userToModify != null){
                boolean needToUpdateSCIM = false;
                for(String attribute : mapping.getAccesslogScimAttributes()){
                    String attrVal = mapping.resolveFromAccesslog(attribute, accesslogEntry);
                    if(attrVal == null){
                        continue;
                    }else if(userToModify.attrHasValue(attribute,attrVal)){
                        continue;
                    }
                    needToUpdateSCIM = true;
                    userToModify.addAttribute(attribute, attrVal);
                }
                if(needToUpdateSCIM){
                    if(getVersion().equals("1.1")){
                        userToModify.addAttribute("externalId", accesslogEntry.getUUID());
                    }
                    modifyUser(userToModify);
                    lastResponse.setAccesslogID(accesslogEntry.getAccesslogID());
                    lastResponse.setAttempt(1);
                    AuditDB.getInstance().insert(lastResponse);
                    if(lastResponse.getResponseCode() != 200){
                        SendAgainEntity sa = new SendAgainEntity();
                        sa.setAccesslogID(accesslogEntry.getAccesslogID());
                        sa.setFailedAttempts(1);
                        sa.setScimServer(acronym);
                        AuditDB.getInstance().insert(sa);
                    }
                }
            }else{
                SendAgainEntity sa = new SendAgainEntity();
                sa.setAccesslogID(accesslogEntry.getAccesslogID());
                sa.setFailedAttempts(1);
                sa.setScimServer(acronym);
                AuditDB.getInstance().insert(sa);
            }
        }else{
            throw new Exception("Accesslog with UUID '"+accesslogEntry.getUUID()+"' has unsupported reqType '"+accesslogEntry.getReqType());
        }
    }
    
    public void resendRequest(SendAgainEntity sa) throws Exception{
        AccesslogEntry accesslogEntry = new AccesslogEntry(sa.getAccesslogID());
        if(accesslogEntry.getReqType().equals("add")){
            ScimServerResource resource = generateEmptyResource();
            resource.addAttribute("externalId", accesslogEntry.getUUID());
            for(String attribute : mapping.getScimAttributes()){
                resource.addAttribute(attribute, mapping.resolveFromAccesslog(attribute, accesslogEntry));
            }

            createUser(resource);
            lastResponse.setAccesslogID(accesslogEntry.getAccesslogID());
            lastResponse.setAttempt(sa.getFailedAttempts()+1);
            AuditDB.getInstance().insert(lastResponse);
            if(lastResponse.getResponseCode() != 200){
                sa.setFailedAttempts(sa.getFailedAttempts()+1);
                AuditDB.getInstance().update(sa);
            }else{
                AuditDB.getInstance().delete(sa);
            }
        }else if(accesslogEntry.getReqType().equals("delete")){
            ScimServerResource userToDelete = fetchUserWithExternalID(accesslogEntry.getUUID());
            lastResponse.setAccesslogID(accesslogEntry.getAccesslogID());
            lastResponse.setAttempt(sa.getFailedAttempts()+1);
            AuditDB.getInstance().insert(lastResponse);
            if(userToDelete != null){
                deleteUser(userToDelete);
                lastResponse.setAccesslogID(accesslogEntry.getAccesslogID());
                lastResponse.setAttempt(sa.getFailedAttempts()+1);
                AuditDB.getInstance().insert(lastResponse);
                if(lastResponse.getResponseCode() == 200){
                    List<SendAgainEntity> obsoleteSendAgains = AuditDB.getInstance().select("SELECT sa FROM SendAgainEntity sa,AccesslogEntryEntity ae WHERE ae.accesslogID=sa.accesslogID AND (ae.reqEndTime < "+accesslogEntry.getReqStartTime()+" OR (ae.reqEndTime = "+accesslogEntry.getReqStartTime()+" AND ae.reqEndCount > "+accesslogEntry.getReqStartCount()+"))", SendAgainEntity.class);
                    for(SendAgainEntity obsSA : obsoleteSendAgains){
                        AuditDB.getInstance().delete(obsSA);
                    }
                    AuditDB.getInstance().delete(sa);
                }else{
                    sa.setFailedAttempts(sa.getFailedAttempts()+1);
                    AuditDB.getInstance().update(sa);
                }
            }else{
                sa.setFailedAttempts(sa.getFailedAttempts()+1);
                AuditDB.getInstance().insert(sa);
            }
        }else if(accesslogEntry.getReqType().equals("modify")){
            ScimServerResource userToModify = fetchUserWithExternalID(accesslogEntry.getUUID());
            lastResponse.setAccesslogID(accesslogEntry.getAccesslogID());
            lastResponse.setAttempt(sa.getFailedAttempts()+1);
            AuditDB.getInstance().insert(lastResponse);
            if(userToModify != null){
                boolean needToUpdateSCIM = false;
                for(String attribute : mapping.getAccesslogScimAttributes()){
                    String attrVal = mapping.resolveFromAccesslog(attribute, accesslogEntry);
                    if(attrVal == null){
                        continue;
                    }else if(userToModify.attrHasValue(attribute,attrVal)){
                        continue;
                    }
                    needToUpdateSCIM = true;
                    userToModify.addAttribute(attribute, attrVal);
                }
                if(needToUpdateSCIM){
                    if(getVersion().equals("1.1")){
                        userToModify.addAttribute("externalId", accesslogEntry.getUUID());
                    }
                    modifyUser(userToModify);
                    lastResponse.setAccesslogID(accesslogEntry.getAccesslogID());
                    lastResponse.setAttempt(sa.getFailedAttempts()+1);
                    AuditDB.getInstance().insert(lastResponse);
                    if(lastResponse.getResponseCode() != 200){
                        sa.setFailedAttempts(sa.getFailedAttempts()+1);
                        AuditDB.getInstance().update(sa);
                    }else{
                        AuditDB.getInstance().delete(sa);
                    }
                }
            }else{
                sa.setFailedAttempts(sa.getFailedAttempts()+1);
                AuditDB.getInstance().update(sa);
            }
        }else{
            throw new Exception("Accesslog with UUID '"+accesslogEntry.getUUID()+"' has unsupported reqType '"+accesslogEntry.getReqType());
        }
    }
    
    private ScimServerResource fetchUserWithExternalID(String externalID){
        Collection<ScimServerResource> searchResults = search("externalId eq \""+externalID+"\"");
        if(lastResponse.getResponseCode() == 200){
            if(searchResults.isEmpty()){
                lastResponse.setErrorType("nonScimError");
                lastResponse.setErrorDetails("User with externalID '"+externalID+"' was not found on identity server '"+acronym+"'");
                return null;
            }else if(searchResults.size() == 1){
                return searchResults.iterator().next();
            }else{
                lastResponse.setErrorType("nonScimError");
                lastResponse.setErrorDetails("Multiple users with externalID '"+externalID+"' were found in scim server '"+acronym+"'");
                return null;
            }
        }else{
            return null;
        }
    }
    
    public abstract ScimServerResource generateEmptyResource();
    
    /*Performs a 'create' scim request to the server. On success the resource
      passed as a parameter is created on the server. Sets lastResponse with
      request/response details*/
    public abstract void createUser(ScimServerResource resource);
    
    /*Performs a 'delete' scim request to the server. On success the resource
      passed as a parameter is removed from the server. Sets lastResponss with
      request/response details*/
    public abstract void deleteUser(ScimServerResource resource);
    
    /*Performs an 'update' scim request to the server. On success, the resource
      described by the passed parameter overrides the corresponding resource on
      the server. Sets lastResponse with request/resposne details*/
    public abstract void modifyUser(ScimServerResource updatedResource);
    
    /*Performs a 'retrieve' scim request to the server and constructs a user
      resource from the response. Returns the constructed resource on success,
      or null on failure. Sets lastResponse with request/response details.*/
    public abstract ScimServerResource getUser(String userID);
    
    /*Performs a 'search' scim request to the server. If successful, constructs
      a server resource for each search result and returns a Collection of those
      resources. Returns null on failure. Sets lastResponse with details.*/
    public abstract Collection<ScimServerResource> search(String searchFilter);
    
    public abstract String getVersion();
    
    /*Inserts Identity Server data into db and returns an Entity object to that
      data. If the data already exist on the db, they are not inserted again and
      the Entity object gets populated with the existing db data.*/
    public SCIMServerEntity persist() throws Exception{
        DBManager db = AuditDB.getInstance();
        db.activate();
        List<SCIMServerEntity> searchRes = db.select("SELECT ise FROM SCIMServerEntity ise WHERE ise.acronym='"+acronym+"'", SCIMServerEntity.class);
        SCIMServerEntity entity = null;
        if(searchRes.isEmpty()){
            entity = new SCIMServerEntity();
            entity.setAcronym(acronym);
            entity.setAuthenticationMethod(authMethod);
            entity.setScimVersion(this.getVersion());
            db.insert(entity);
        }else if(searchRes.size() != 1){
            db.inactivate();
            throw new Exception("Identity Server with acronym '"+acronym+"' exists multiple times in the database");
        }
        db.inactivate();
        return entity;
    }
    
    public String getAcronym(){
        return acronym;
    }
    
    public AccesslogToScimMapping getMapping(){
        return mapping;
    }
    
    public ScimResponseEntity getLastResponse(){
        return lastResponse;
    }
}
