package gr.gunet.accesslogtoscim.idserver;

import com.unboundid.scim.data.UserResource;
import com.unboundid.scim.sdk.OAuthToken;
import com.unboundid.scim.sdk.Resources;
import com.unboundid.scim.sdk.SCIMEndpoint;
import com.unboundid.scim.sdk.SCIMException;
import com.unboundid.scim.sdk.SCIMService;
import gr.gunet.accesslogtoscim.auditdb.ScimResponseEntity;
import gr.gunet.accesslogtoscim.resource.Scim1_1Resource;
import java.net.URI;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.core.MediaType;
import gr.gunet.accesslogtoscim.resource.ScimServerResource;

public class Scim1_1Server extends ScimServer{
    private final SCIMService scimService;
    
    public Scim1_1Server(String identityServerAcronym) throws Exception{
        super(identityServerAcronym);
        if(authMethod.equals("token")){
            scimService = new SCIMService(new URI(serverEndpoint), new OAuthToken(token));
            scimService.setAcceptType(MediaType.APPLICATION_JSON_TYPE);
        }else if(authMethod.equals("password")){
            scimService = new SCIMService(new URI(serverEndpoint), username, password);
            scimService.setAcceptType(MediaType.APPLICATION_JSON_TYPE);
        }else{
            throw new Exception("Identity Server '"+identityServerAcronym+"' requires unsupported AuthenticationMethod '"+authMethod+"'");
        }
    }

    @Override
    public ScimServerResource generateEmptyResource() {
        UserResource newResource = scimService.getUserEndpoint().newResource();
        ScimServerResource newScimResource = new Scim1_1Resource(newResource);
        return newScimResource;
    }

    @Override
    public ScimServerResource getUser(String userID){
        SCIMEndpoint<UserResource> scimUserEndpoint = scimService.getUserEndpoint();
        lastResponse = new ScimResponseEntity();
        lastResponse.setReceivingServer(acronym);
        lastResponse.setRequestType("retrieve");
        lastResponse.setRequestMethod("GET");
        lastResponse.setReqSentTimestamp(new Timestamp(System.currentTimeMillis()));
        UserResource user = null;
        try{
            user = scimUserEndpoint.get(userID);
            lastResponse.setResReceivedTimestamp(new Timestamp(System.currentTimeMillis()));
            lastResponse.setResponseCode(200);
        }catch(SCIMException se){
            lastResponse.setResReceivedTimestamp(new Timestamp(System.currentTimeMillis()));
            lastResponse.setResponseCode(se.getStatusCode());
            lastResponse.setErrorDetails(se.getMessage());
        }catch(Exception e){
            lastResponse.setResponseCode(0);
            lastResponse.setErrorType("nonScimError");
            lastResponse.setErrorDetails(e.getMessage());
        }
        return new Scim1_1Resource(user);
    }

    @Override
    public void createUser(ScimServerResource resource){
        SCIMEndpoint<UserResource> endpoint = scimService.getUserEndpoint();
        lastResponse = new ScimResponseEntity();
        lastResponse.setRequestType("create");
        lastResponse.setRequestMethod("POST");
        lastResponse.setReceivingServer(acronym);
        lastResponse.setBody(resource.resourceBodyToString());
        lastResponse.setReqSentTimestamp(new Timestamp(System.currentTimeMillis()));
        try{
            endpoint.create(resource.getAsScim1Resource());
            lastResponse.setResReceivedTimestamp(new Timestamp(System.currentTimeMillis()));
            lastResponse.setResponseCode(200);
        }catch(SCIMException se){
            lastResponse.setResReceivedTimestamp(new Timestamp(System.currentTimeMillis()));
            lastResponse.setResponseCode(se.getStatusCode());
            lastResponse.setErrorDetails(se.getMessage());
        }catch(Exception e){
            lastResponse.setResponseCode(0);
            lastResponse.setErrorType("nonScimError");
            lastResponse.setErrorDetails(e.getMessage());
        }
    }

    @Override
    public void deleteUser(ScimServerResource resource){
        SCIMEndpoint<UserResource> scimUserEndpoint = scimService.getUserEndpoint();
        lastResponse = new ScimResponseEntity();
        lastResponse.setRequestMethod("DELETE");
        lastResponse.setRequestType("delete");
        lastResponse.setReceivingServer(acronym);
        lastResponse.setReqSentTimestamp(new Timestamp(System.currentTimeMillis()));
        try{
            scimUserEndpoint.delete(resource.getAsScim1Resource());
            lastResponse.setResReceivedTimestamp(new Timestamp(System.currentTimeMillis()));
            lastResponse.setResponseCode(200);
        }catch(SCIMException se){
            lastResponse.setResReceivedTimestamp(new Timestamp(System.currentTimeMillis()));
            lastResponse.setResponseCode(se.getStatusCode());
            lastResponse.setErrorDetails(se.getMessage());
        }catch(Exception e){
            lastResponse.setResponseCode(0);
            lastResponse.setErrorType("nonScimError");
            lastResponse.setErrorDetails(e.getMessage());
        }
    }

    @Override
    public void modifyUser(ScimServerResource updatedResource){
        SCIMEndpoint<UserResource> scimUserEndpoint = scimService.getUserEndpoint();
        lastResponse = new ScimResponseEntity();
        lastResponse.setRequestMethod("PUT");
        lastResponse.setRequestType("modify");
        lastResponse.setReceivingServer(acronym);
        lastResponse.setBody(updatedResource.resourceBodyToString());
        lastResponse.setReqSentTimestamp(new Timestamp(System.currentTimeMillis()));
        try{
            scimUserEndpoint.update(updatedResource.getAsScim1Resource());
            lastResponse.setResReceivedTimestamp(new Timestamp(System.currentTimeMillis()));
            lastResponse.setResponseCode(200);
        }catch(SCIMException se){
            lastResponse.setResReceivedTimestamp(new Timestamp(System.currentTimeMillis()));
            lastResponse.setResponseCode(se.getStatusCode());
            lastResponse.setErrorDetails(se.getMessage());
        }catch(Exception e){
            lastResponse.setResponseCode(0);
            lastResponse.setErrorType("nonScimError");
            lastResponse.setErrorDetails(e.getMessage());
        }
    }

    @Override
    public Collection<ScimServerResource> search(String searchFilter){
        SCIMEndpoint<UserResource> scimUserEndpoint = scimService.getUserEndpoint();
        List<ScimServerResource> searchResults = new LinkedList();
        Resources<UserResource> rawResults;
        lastResponse = new ScimResponseEntity();
        lastResponse.setRequestType("search");
        lastResponse.setRequestMethod("GET");
        lastResponse.setReceivingServer(acronym);
        lastResponse.setReqSentTimestamp(new Timestamp(System.currentTimeMillis()));
        try{
            rawResults = scimUserEndpoint.query(searchFilter);
            lastResponse.setResReceivedTimestamp(new Timestamp(System.currentTimeMillis()));
            lastResponse.setResponseCode(200);
        }catch(SCIMException se){
            lastResponse.setResReceivedTimestamp(new Timestamp(System.currentTimeMillis()));
            lastResponse.setResponseCode(se.getStatusCode());
            lastResponse.setErrorDetails(se.getMessage());
            return null;
        }catch(Exception e){
            lastResponse.setResponseCode(0);
            lastResponse.setErrorType("nonScimError");
            lastResponse.setErrorDetails(e.getMessage());
            return null;
        }
        for(UserResource baseResource : rawResults){
            ScimServerResource resource = new Scim1_1Resource(baseResource);
            searchResults.add(resource);
        }
        return searchResults;
    }
    
    @Override
    public String getVersion(){
        return "1.1";
    }
}
