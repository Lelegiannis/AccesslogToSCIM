package gr.gunet.accesslogtoscim.resource;

import com.unboundid.scim.data.UserResource;
import com.unboundid.scim.sdk.ComplexValue;
import com.unboundid.scim.sdk.SCIMAttribute;
import com.unboundid.scim.sdk.SCIMAttributeValue;
import com.unboundid.scim.sdk.SimpleValue;
import com.unboundid.scim2.common.GenericScimResource;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

public class Scim1_1Resource implements ScimServerResource{
    private UserResource resource;
    
    public Scim1_1Resource(UserResource resource){
        this.resource = resource;
    }

    @Override
    public ScimServerResource addAttribute(String attributePath, String primitiveValue) throws Exception{
        if(attributePath.contains("[")){
            String[] dotSplit = attributePath.split("\\.");
            if(dotSplit.length > 2){
                throw new Exception("Attributes with complex subattributes are not allowed in SCIM1.1");
            }else if(dotSplit.length == 1){
                throw new Exception("Array attributes can contain only complex values in SCIM1.1");
            }
            String[] arraySplit = dotSplit[0].split("\\[");
            String attributeName = arraySplit[0];
            Integer position = Integer.parseInt(arraySplit[1].replace("]", ""));
            String subAttributeName = dotSplit[1];
            if(subAttributeName.contains("[")){
                throw new Exception("Subattributes cannot be arrays in SCIM1.1");
            }
            Collection<ComplexValue> arrayAttr = resource.getMultiValuedAttribute(attributeName);
            if(arrayAttr == null){
                arrayAttr = new LinkedList();
            }
            int iter = 0;
            ComplexValue subAttr = null;
            for(ComplexValue val : arrayAttr){
                if(iter == position){
                    subAttr = val;
                    subAttr.put(subAttributeName, createPrimitive(primitiveValue));
                }
            }
            if(subAttr == null){
                subAttr = new ComplexValue();
                subAttr.put(subAttributeName, createPrimitive(primitiveValue));
                arrayAttr.add(subAttr);
                resource.setMultiValuedAttribute(attributeName, arrayAttr);
            }
        }else if(attributePath.contains(".")){
            String[] dotSplit = attributePath.split("\\.");
            if(dotSplit.length > 2){
                throw new Exception("Attributes with complex subattributes are not allowed in SCIM1.1");
            }
            String attributeName = dotSplit[0];
            String subAttributeName = dotSplit[1];
            ComplexValue val = resource.getComplexAttributeValue(attributeName);
            if(val == null){
                val = new ComplexValue();
            }
            val.put(subAttributeName, createPrimitive(primitiveValue));
            resource.setComplexAttribute(attributeName, val);
        }else{
            resource.setSimpleAttribute(attributePath, createPrimitive(primitiveValue));
        }
        return this;
    }
    
    private SimpleValue createPrimitive(String primitiveValue) throws Exception{
        SimpleValue value;
        if(primitiveValue.matches("[0-9]+")){
            value = new SimpleValue(Integer.parseInt(primitiveValue));
        }else if(primitiveValue.equals("true")){
            value = new SimpleValue(Boolean.TRUE);
        }else if(primitiveValue.equals("false")){
            value = new SimpleValue(Boolean.FALSE);
        }else{
            value = new SimpleValue(primitiveValue.replaceAll("\"",""));
        }
        return value;
    }

    @Override
    public GenericScimResource getAsScim2Resource() throws Exception{
        throw new Exception("Version missmatch: called 'getAsScim2Resource' method on a Scim1_1Resource object");
    }

    @Override
    public UserResource getAsScim1Resource() {
        return resource;
    }

    @Override
    public String resourceBodyToString() {
        String retVal = "";
        for(String schema : resource.getScimObject().getSchemas()){
            retVal += "\""+schema+"\": {\n";
            boolean firstLoop = true;
            for(SCIMAttribute attr : resource.getScimObject().getAttributes(schema)){
                if(!firstLoop){
                    retVal += ",\n";
                }else{
                    firstLoop = false;
                }
                retVal += scimAttributeToString(attr);
            }
            retVal += "}";
        }
        if(!retVal.startsWith("{")){
            retVal = "{"+retVal+"}";
        }
        return retVal;
    }
    
    @Override
    public boolean attrHasValue(String attributePath, String primitiveValue) throws Exception{
         if(attributePath.contains("[")){
            String[] dotSplit = attributePath.split("\\.");
            if(dotSplit.length > 2){
                throw new Exception("Attributes with complex subattributes are not allowed in SCIM1.1");
            }else if(dotSplit.length == 1){
                throw new Exception("Array attributes can contain only complex values in SCIM1.1");
            }
            String[] arraySplit = dotSplit[0].split("\\[");
            String attributeName = arraySplit[0];
            Integer position = Integer.parseInt(arraySplit[1].replace("]", ""));
            String subAttributeName = dotSplit[1];
            if(subAttributeName.contains("[")){
                throw new Exception("Subattributes cannot be arrays in SCIM1.1");
            }
            Collection<ComplexValue> arrayAttr = resource.getMultiValuedAttribute(attributeName);
            if(arrayAttr == null){
                return false;
            }
            int iter = 0;
            for(ComplexValue val : arrayAttr){
                if(iter == position){
                    SimpleValue existingValue = val.get(subAttributeName);
                    if(existingValue == null){
                        return false;
                    }else{
                        return valuesMatch(existingValue,primitiveValue);
                    }
                }
            }
            return false;
        }else if(attributePath.contains(".")){
            String[] dotSplit = attributePath.split("\\.");
            if(dotSplit.length > 2){
                throw new Exception("Attributes with complex subattributes are not allowed in SCIM1.1");
            }
            String attributeName = dotSplit[0];
            String subAttributeName = dotSplit[1];
            ComplexValue val = resource.getComplexAttributeValue(attributeName);
            if(val == null){
                return false;
            }else{
                SimpleValue existingValue = val.get(subAttributeName);
                if(existingValue == null){
                    return false;
                }else{
                    return valuesMatch(existingValue,primitiveValue);
                }
            }
        }else{
            SimpleValue existingValue = resource.getSimpleAttributeValue(attributePath);
            if(existingValue == null){
                return false;
            }else{
                return valuesMatch(existingValue,primitiveValue);
            }
        }
    }
    
    private boolean valuesMatch(SimpleValue sVal,String val){
        SimpleValue temp;
        if(val.equals("true")){
            temp = new SimpleValue(Boolean.TRUE);
        }else if(val.equals("false")){
            temp = new SimpleValue(Boolean.FALSE);
        }else if(val.matches("[0-9]+")){
            temp = new SimpleValue(Integer.parseInt(val));
        }else{
            temp = new SimpleValue(val.replaceAll("\"",""));
        }
        return temp.equals(sVal);
    }
    
    private String scimAttributeToString(SCIMAttribute scimAttribute){
        String retVal = "\""+scimAttribute.getName()+"\":";
        SCIMAttributeValue[] values = scimAttribute.getValues();
        if(values.length == 0){
            retVal += "null";
            return retVal;
        }
        if(values.length > 1){
            retVal += "[";
        }
        boolean firstLoop = true;
        for(SCIMAttributeValue val : values){
            if(!firstLoop){
                retVal += ",";
            }else{
                firstLoop = false;
            }
            String attrVal = scimValToString(val);
            if(!attrVal.equals("true") && !attrVal.equals("false") && !attrVal.matches("[0-9]+") && !attrVal.startsWith("{") && !attrVal.startsWith("[")){
                retVal += "\""+attrVal+"\"";
            }else{
                retVal += attrVal;
            }
        }
        if(values.length > 1){
            retVal += "]";
        }
        return retVal;
    }
    
    private String scimValToString(SCIMAttributeValue val){
        if(val.isComplex()){
            String retVal = "{";
            boolean firstLoop = true;
            for(Map.Entry<String,SCIMAttribute> subAttr : val.getAttributes().entrySet()){
                if(!firstLoop){
                    retVal += ",";
                }else{
                    firstLoop = false;
                }
                retVal += scimAttributeToString(subAttr.getValue());
            }
            return retVal+"}";
        }else{
            return val.getStringValue();
        }
    }
}
