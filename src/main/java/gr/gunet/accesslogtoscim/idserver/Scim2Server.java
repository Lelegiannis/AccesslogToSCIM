package gr.gunet.accesslogtoscim.idserver;

import com.unboundid.scim2.client.ScimService;
import com.unboundid.scim2.common.GenericScimResource;
import com.unboundid.scim2.common.exceptions.ScimException;
import com.unboundid.scim2.common.messages.ErrorResponse;
import com.unboundid.scim2.common.messages.ListResponse;
import com.unboundid.scim2.common.types.UserResource;
import gr.gunet.accesslogtoscim.auditdb.ScimResponseEntity;
import gr.gunet.accesslogtoscim.resource.Scim2Resource;
import java.net.URI;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.oauth2.OAuth2ClientSupport;
import gr.gunet.accesslogtoscim.resource.ScimServerResource;

public class Scim2Server extends ScimServer{
    private final ScimService scimService;
    
    public Scim2Server(String identityServerAcronym) throws Exception{
        super(identityServerAcronym);
        if(authMethod.equals("token")){
            Client client = ClientBuilder.newClient().register(OAuth2ClientSupport.feature(token));
            WebTarget target = client.target(new URI(serverEndpoint));
            scimService = new ScimService(target);
        }else if(authMethod.equals("password")){
            HttpAuthenticationFeature basicAuthFeature = HttpAuthenticationFeature.basicBuilder().credentials(username,password).build();
            ClientConfig config = new ClientConfig();
            config.register(basicAuthFeature);
            
            Client client = ClientBuilder.newClient(config);
            WebTarget target = client.target(new URI(serverEndpoint));
            scimService = new ScimService(target);
        }else{
            throw new Exception("Identity Server '"+identityServerAcronym+"' requires unsupported AuthenticationMethod '"+authMethod+"'");
        }
    }
    
    @Override
    public ScimServerResource generateEmptyResource(){
        UserResource userResource = new UserResource();
        GenericScimResource baseResource = userResource.asGenericScimResource();
        return new Scim2Resource(baseResource);
    }
    
    @Override
    public ScimServerResource getUser(String userID){
        UserResource res = null;
        lastResponse = new ScimResponseEntity();
        lastResponse.setRequestMethod("GET");
        lastResponse.setRequestType("retrieve");
        lastResponse.setReceivingServer(acronym);
        lastResponse.setReqSentTimestamp(new Timestamp(System.currentTimeMillis()));
        try{
            res = scimService.retrieve("Users", userID, UserResource.class);
            lastResponse.setResReceivedTimestamp(new Timestamp(System.currentTimeMillis()));
            lastResponse.setResponseCode(200);
        }catch(ScimException se){
            lastResponse.setResReceivedTimestamp(new Timestamp(System.currentTimeMillis()));
            ErrorResponse err = se.getScimError();
            lastResponse.setResponseCode(err.getStatus());
            lastResponse.setErrorType(err.getScimType());
            lastResponse.setErrorDetails(err.getDetail());
        }catch(Exception e){
            lastResponse.setResponseCode(0);
            lastResponse.setErrorType("nonScimError");
            lastResponse.setErrorDetails(e.getMessage());
        }
        if(res == null){
            return null;
        }
        return new Scim2Resource(res.asGenericScimResource());
    }
    
    @Override
    public void createUser(ScimServerResource resource){
        lastResponse = new ScimResponseEntity();
        lastResponse.setRequestMethod("POST");
        lastResponse.setRequestType("create");
        lastResponse.setReceivingServer(acronym);
        lastResponse.setBody(resource.resourceBodyToString());
        lastResponse.setReqSentTimestamp(new Timestamp(System.currentTimeMillis()));
        try{
            scimService.create("Users", resource.getAsScim2Resource());
            lastResponse.setResReceivedTimestamp(new Timestamp(System.currentTimeMillis()));
            lastResponse.setResponseCode(200);
        }catch(ScimException se){
            lastResponse.setResReceivedTimestamp(new Timestamp(System.currentTimeMillis()));
            ErrorResponse err = se.getScimError();
            lastResponse.setResponseCode(err.getStatus());
            lastResponse.setErrorType(err.getScimType());
            lastResponse.setErrorDetails(err.getDetail());
        }catch(Exception e){
            lastResponse.setResponseCode(0);
            lastResponse.setErrorType("nonScimError");
            lastResponse.setErrorDetails(e.getMessage());
        }
    }
    
    @Override
    public void deleteUser(ScimServerResource resource){
        lastResponse = new ScimResponseEntity();
        lastResponse.setRequestMethod("DELETE");
        lastResponse.setRequestType("delete");
        lastResponse.setReceivingServer(acronym);
        lastResponse.setReqSentTimestamp(new Timestamp(System.currentTimeMillis()));
        try{
            scimService.delete("Users",resource.getAsScim2Resource().getId());
            lastResponse.setResReceivedTimestamp(new Timestamp(System.currentTimeMillis()));
            lastResponse.setResponseCode(200);
        }catch(ScimException se){
            lastResponse.setResReceivedTimestamp(new Timestamp(System.currentTimeMillis()));
            ErrorResponse err = se.getScimError();
            lastResponse.setResponseCode(err.getStatus());
            lastResponse.setErrorType(err.getScimType());
            lastResponse.setErrorDetails(err.getDetail());
        }catch(Exception e){
            lastResponse.setResponseCode(0);
            lastResponse.setErrorType("nonScimError");
            lastResponse.setErrorDetails(e.getMessage());
        }
    }
    
    @Override
    public void  modifyUser(ScimServerResource updatedResource){
        lastResponse = new ScimResponseEntity();
        lastResponse.setReceivingServer(acronym);
        lastResponse.setRequestMethod("PUT");
        lastResponse.setRequestType("modify");
        lastResponse.setBody(updatedResource.resourceBodyToString());
        lastResponse.setReqSentTimestamp(new Timestamp(System.currentTimeMillis()));
        try{
            scimService.replace(updatedResource.getAsScim2Resource());
            lastResponse.setResReceivedTimestamp(new Timestamp(System.currentTimeMillis()));
            lastResponse.setResponseCode(200);
        }catch(ScimException se){
            lastResponse.setResReceivedTimestamp(new Timestamp(System.currentTimeMillis()));
            ErrorResponse err = se.getScimError();
            lastResponse.setResponseCode(err.getStatus());
            lastResponse.setErrorType(err.getScimType());
            lastResponse.setErrorDetails(err.getDetail());
        }catch(Exception e){
            lastResponse.setResponseCode(0);
            lastResponse.setErrorType("nonScimError");
            lastResponse.setErrorDetails(e.getMessage());
        }
    }
    
    @Override
    public Collection<ScimServerResource> search(String searchFilter){
        List<ScimServerResource> retVal = new LinkedList();
        lastResponse = new ScimResponseEntity();
        lastResponse.setRequestMethod("GET");
        lastResponse.setRequestType("search");
        lastResponse.setReceivingServer(acronym);
        lastResponse.setReqSentTimestamp(new Timestamp(System.currentTimeMillis()));
        ListResponse<UserResource> rawResults;
        try{
            rawResults = scimService.search("Users",searchFilter,UserResource.class);
            lastResponse.setResReceivedTimestamp(new Timestamp(System.currentTimeMillis()));
            lastResponse.setResponseCode(200);
        }catch(ScimException se){
            lastResponse.setResReceivedTimestamp(new Timestamp(System.currentTimeMillis()));
            ErrorResponse err = se.getScimError();
            lastResponse.setResponseCode(err.getStatus());
            lastResponse.setErrorType(err.getScimType());
            lastResponse.setErrorDetails(err.getDetail());
            return null;
        }catch(Exception e){
            lastResponse.setResponseCode(0);
            lastResponse.setErrorType("nonScimError");
            lastResponse.setErrorDetails(e.getMessage());
            return null;
        }
        for(UserResource res : rawResults){
            retVal.add(new Scim2Resource(res.asGenericScimResource()));
        }
        return retVal;
    }
    
    @Override
    public String getVersion(){
        return "2";
    }
}
