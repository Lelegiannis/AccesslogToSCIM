package gr.gunet.accesslogtoscim.idserver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collection;
import java.util.HashMap;

public class AccesslogToScimMapping {
    private final HashMap<String,String> mapping;
    private final HashMap<String,String> accesslogMapping;
    
    public AccesslogToScimMapping(String mappingFileName) throws Exception{
        BufferedReader reader = new BufferedReader(new FileReader(mappingFileName));
        mapping = new HashMap();
        accesslogMapping = new HashMap();
        
        String mapping;
        while((mapping = reader.readLine()) != null){
            if(mapping.startsWith("#")){
                continue;
            }
            String[] mappingSplit = mapping.split("->");
            String value = mappingSplit[1].trim();
            String scimAttribute = mappingSplit[0].trim();
            this.mapping.put(scimAttribute,value);
            if(value.startsWith("accesslog::")){
                this.accesslogMapping.put(scimAttribute, value);
            }
        }
    }
    
    public String resolveFromAccesslog(String scimAttribute,AccesslogEntry accesslog){
        String mappedValue = mapping.get(scimAttribute);
        if(mappedValue == null) return null;
        
        if(mappedValue.startsWith("accesslog::")){
            String ldapAttribute = mappedValue.replace("accesslog::","");
            return accesslog.getReqModAttrVal(ldapAttribute);
        }else{
            return mappedValue;
        }
    }
    
    public Collection<String> getScimAttributes(){
        return mapping.keySet();
    }
    
    public Collection<String> getAccesslogScimAttributes(){
        return accesslogMapping.keySet();
    }
}
