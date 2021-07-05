package gr.gunet.accesslogtoscim.resource;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unboundid.scim.data.UserResource;
import com.unboundid.scim2.common.GenericScimResource;

public class Scim2Resource implements ScimServerResource{
    private final GenericScimResource resource;
    public Scim2Resource(GenericScimResource resource){
        this.resource = resource;
    }

    @Override
    public ScimServerResource addAttribute(String attributePath, String primitiveValue) throws Exception {
        if(attributePath.contains("[")){
            ObjectNode node = resource.getObjectNode();
            String[] attributesInPath = attributePath.split("\\.",2);
            while(attributesInPath.length > 1){
                String currentAttrName = attributesInPath[0];
                if(currentAttrName.contains("[")){
                    String[] arraySplit = currentAttrName.split("\\[");
                    String arrayName = arraySplit[0];
                    String arrayPosStr = arraySplit[1].replaceAll("[^\\d]","");
                    Integer arrayPos = Integer.parseInt(arrayPosStr);
                    ArrayNode array = node.withArray(arrayName);
                    if(array.size() > arrayPos){
                        node = (ObjectNode) array.get(arrayPos);
                    }else if(array.size() == arrayPos){
                        node = array.insertObject(array.size());
                    }else{
                        throw new Exception("Array size missmatch: array '"+arrayName+"' in path '"+attributePath+"' has a size of "+array.size());
                    }
                }else{
                    node = node.with(currentAttrName);
                }
                attributesInPath = attributesInPath[1].split("\\.",1);
            }
            String attrName = attributesInPath[0];
            addPrimitiveToNode(node,attrName,primitiveValue);
            return this;
        }else{
            return addPrimitive(attributePath,primitiveValue);
        }
    }
    
    private void addPrimitiveToNode(ObjectNode node, String attributeName, String primitiveValue){
        if(primitiveValue.matches("[0-9]+")){
            node.put(attributeName,Integer.parseInt(primitiveValue));
        }else if(primitiveValue.equals("true")){
            node.put(attributeName,Boolean.TRUE);
        }else if(primitiveValue.equals("false")){
            node.put(attributeName,Boolean.FALSE);
        }else{
            node.put(attributeName,primitiveValue.replaceAll("\"",""));
        }
    }
    
    private ScimServerResource addPrimitive(String attributePath, String primitiveValue) throws Exception{
        if(primitiveValue.matches("[0-9]+")){
            resource.replaceValue(attributePath,Integer.parseInt(primitiveValue));
        }else if(primitiveValue.equals("true")){
            resource.replaceValue(attributePath, Boolean.TRUE);
        }else if(primitiveValue.equals("false")){
            resource.replaceValue(attributePath, Boolean.FALSE);
        }else{
            resource.replaceValue(attributePath, primitiveValue.replaceAll("\"",""));
        }
        return this;
    }
    
    @Override
    public boolean attrHasValue(String attributePath,String primitiveValue) throws Exception{
        if(attributePath.contains("[")){
            ObjectNode node = resource.getObjectNode();
            String[] attributesInPath = attributePath.split("\\.",2);
            while(attributesInPath.length > 1){
                String currentAttrName = attributesInPath[0];
                if(currentAttrName.contains("[")){
                    String[] arraySplit = currentAttrName.split("\\[");
                    String arrayName = arraySplit[0];
                    String arrayPosStr = arraySplit[1].replaceAll("[^\\d]","");
                    Integer arrayPos = Integer.parseInt(arrayPosStr);
                    ArrayNode array = node.withArray(arrayName);
                    if(array.size() > arrayPos){
                        node = (ObjectNode) array.get(arrayPos);
                    }else if(array.size() == arrayPos){
                        node = array.insertObject(array.size());
                    }else{
                        throw new Exception("Array size missmatch: array '"+arrayName+"' in path '"+attributePath+"' has a size of "+array.size());
                    }
                }else{
                    node = node.with(currentAttrName);
                }
                attributesInPath = attributesInPath[1].split("\\.",2);
            }
            String attrName = attributesInPath[0];
            if(node.hasNonNull(attrName)){
                if(primitiveValue.equals("true")){
                    return node.get(attrName).asBoolean(false);
                }else if(primitiveValue.equals("false")){
                    return !node.get(attrName).asBoolean(true);
                }else if(primitiveValue.matches("[0-9]+")){
                    if(node.get(attrName).isInt()){
                        return node.get(attrName).asInt() == Integer.parseInt(primitiveValue);
                    }else{
                        return false;
                    }
                }else{
                    String existingValue = node.asText();
                    return primitiveValue.replaceAll("\"","").equals(existingValue);
                }
            }else{
                return false;
            }
        }else{
            return valueMatches(attributePath,primitiveValue);
        }
    }
    
    private boolean valueMatches(String attributePath,String primitiveValue){
        if(primitiveValue.matches("[0-9]+")){
            Integer existingValue;
            try{
                existingValue = resource.getIntegerValue(attributePath);
            }catch(Exception e){
                return false;
            }
            return Integer.parseInt(primitiveValue) == existingValue;
        }else if(primitiveValue.equals("true")){
            Boolean existingValue;
            try{
                existingValue = resource.getBooleanValue(attributePath);
            }catch(Exception e){
                return false;
            }
            return existingValue;
        }else if(primitiveValue.equals("false")){
            Boolean existingValue;
            try{
                existingValue = resource.getBooleanValue(attributePath);
            }catch(Exception e){
                return false;
            }
            return !existingValue;
        }else{
            String existingValue;
            try{
                existingValue = resource.getStringValue(attributePath);
            }catch(Exception e){
                return false;
            }
            return primitiveValue.replaceAll("\"","").equals(existingValue);
        }
    }

    @Override
    public GenericScimResource getAsScim2Resource() {
        return this.resource;
    }

    @Override
    public UserResource getAsScim1Resource() throws Exception{
        throw new Exception("Version missmatch: called 'getAsScim1Resource' method on a Scim2Resource object");
    }
    
    @Override
    public String resourceBodyToString(){
        return resource.toString();
    }
    
}
