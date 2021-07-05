package gr.gunet.accesslogtoscim.resource;

import com.unboundid.scim.data.UserResource;
import com.unboundid.scim2.common.GenericScimResource;

public interface ScimServerResource {
    /*adds the specified attribute to the resource and then returns the object
      with the attribute added.*/
    public ScimServerResource addAttribute(String attributePath, String primitiveValue) throws Exception;
    
    public GenericScimResource getAsScim2Resource() throws Exception;
    
    public UserResource getAsScim1Resource() throws Exception;
    
    /*get a printable form of the json body of the request that will be sent for
     this scim resource. For debug purposes*/
    public String resourceBodyToString();
    
    /*Check if the attribute specified by attributePath has is a primitive 
      attribute with value primitiveValue. If attribute doesn't exists or is not
      a primitive attribute, the method return false.*/
    public boolean attrHasValue(String attributePath, String primitiveValue) throws Exception;
}
