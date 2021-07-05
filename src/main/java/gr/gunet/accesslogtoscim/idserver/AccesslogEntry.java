package gr.gunet.accesslogtoscim.idserver;

import gr.gunet.accesslogtoscim.auditdb.AccesslogEntryEntity;
import gr.gunet.accesslogtoscim.auditdb.AccesslogReqModEntity;
import gr.gunet.accesslogtoscim.auditdb.AuditDB;
import gr.gunet.accesslogtoscim.auditdb.DBManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;

public class AccesslogEntry{
    private final String uuid;
    
    private Integer dbID;
    
    private final String dn;
    private final String reqDN;
    private final String reqStartTime;
    private final String reqStartCount;
    private final String reqEndTime;
    private final String reqEndCount;
    private final String reqType;
    private Collection<String> reqMods;
    private HashMap<String,String> reqModAttributeValues;
    
    public AccesslogEntry(Integer accesslogID) throws Exception{
        List<AccesslogEntryEntity> dbEntities = AuditDB.getInstance().select("SELECT ae FROM AccesslogEntryEntity ae WHERE ae.accesslogID="+accesslogID,AccesslogEntryEntity.class);
        if(dbEntities.isEmpty()){
            throw new Exception("AccesslogID "+accesslogID+" does not exist in database.");
        }else if(dbEntities.size() > 1){
            throw new Exception("AccesslogID "+accesslogID+" exists multiple times in database.");
        }
        
        AccesslogEntryEntity entity = dbEntities.get(0);
        dbID = entity.getAccesslogID();
        
        uuid = entity.getUuid();
        dn = null;
        reqDN = entity.getReqDN();
        reqStartTime = entity.getReqStartTime().toString();
        reqStartCount = entity.getReqStartCount().toString();
        reqEndTime = entity.getReqEndTime().toString();
        reqEndCount = entity.getReqEndCount().toString();
        reqType = entity.getReqType();
        
        reqMods = null;
        reqModAttributeValues = new HashMap();
        List<AccesslogReqModEntity> reqModEntities = AuditDB.getInstance().select("SELECT arm FROM AccesslogReqModEntity arm WHERE arm.accesslogID="+accesslogID,AccesslogReqModEntity.class);
        for(AccesslogReqModEntity reqModEntity : reqModEntities){
            String attrName = reqModEntity.getAttributeName();
            String attrVal = reqModEntity.getValue();
            reqModAttributeValues.put(attrName,attrVal);
        }
    }
    
    public AccesslogEntry(LdapEntry entry) throws Exception{
        dn = entry.getDn();
        LdapAttribute uuidAttr = entry.getAttribute("reqEntryUUID");
        LdapAttribute reqDnAttr = entry.getAttribute("reqDN");
        LdapAttribute startAttr = entry.getAttribute("reqStart");
        LdapAttribute endAttr = entry.getAttribute("reqEnd");
        LdapAttribute typeAttr = entry.getAttribute("reqType");
        LdapAttribute modsAttr = entry.getAttribute("reqMod");

        //Check for missing attributes and throw exceptions when necessary

        dbID = null;
        
        uuid = uuidAttr.getStringValue();
        reqDN = reqDnAttr.getStringValue();
        String[] reqStartSplit = startAttr.getStringValue().split("\\.");
        String[] reqEndSplit = endAttr.getStringValue().split("\\.");
        reqStartTime = reqStartSplit[0].replaceAll("[^0-9]+", "");
        reqStartCount = reqStartSplit[1].replaceAll("[^0-9]+", "");
        reqEndTime = reqEndSplit[0].replaceAll("[^0-9]+", "");
        reqEndCount = reqEndSplit[1].replaceAll("[^0-9]+", "");
        reqType = typeAttr.getStringValue();
        if(modsAttr == null){
            reqMods = null;
        }else{
            reqMods = modsAttr.getStringValues();
            reqModAttributeValues = new HashMap();
            for(String reqMod : reqMods){
                String[] reqModSplit;
                if(reqMod.contains(":+")){
                    reqModSplit = reqMod.split(":\\+");
                }else if(reqMod.contains(":-")){
                    reqModSplit = reqMod.split(":\\-");
                }else{
                    reqModSplit = reqMod.split(":=");
                }
                String attrName = reqModSplit[0].trim();
                String attrVal = reqModSplit[1].trim();
                if(!reqModAttributeValues.containsKey(attrName)){
                    reqModAttributeValues.put(attrName,attrVal);
                }
            }
        }
    }
    
    public boolean isPersisted(){
        return dbID != null;
    }
    
    public AccesslogEntryEntity persist() throws Exception{
        DBManager db = AuditDB.getInstance();
        db.activate();
        AccesslogEntryEntity dbLogEntry = new AccesslogEntryEntity();
        dbLogEntry.setReqDN(reqDN);
        dbLogEntry.setReqStartTime(Long.parseLong(reqStartTime));
        dbLogEntry.setReqStartCount(Integer.parseInt(reqStartCount));
        dbLogEntry.setReqEndTime(Long.parseLong(reqEndTime));
        dbLogEntry.setReqEndCount(Integer.parseInt(reqEndCount));
        dbLogEntry.setReqType(reqType);
        dbLogEntry.setUuid(uuid);
        db.insert(dbLogEntry);
        dbID = dbLogEntry.getAccesslogID();
        if(reqMods != null){
            for(String reqMod : reqMods){
                AccesslogReqModEntity reqModEntity = new AccesslogReqModEntity();
                reqModEntity.setAccesslogID(dbID);
                String[] modSplit = reqMod.split(":",2);
                reqModEntity.setAttributeName(modSplit[0].trim());
                reqModEntity.setValue(modSplit[1].trim().substring(1).trim());
                reqModEntity.setOperation(modSplit[1].trim().substring(0, 1).trim());
                db.insert(reqModEntity);
            }
        }
        db.inactivate();
        return dbLogEntry;
    }
    
    public Integer getAccesslogID(){
        return dbID;
    }
    
    public Collection<String> getReqMods(){
        return reqMods;
    }
    
    public String getReqModAttrVal(String attributeName){
        return reqModAttributeValues.get(attributeName);
    }
    
    public String getReqType(){
        return reqType;
    }
    
    public String getDN(){
        return dn;
    }
    
    public String getReqDN(){
        return reqDN;
    }
    
    public String getReqStartTime(){
        return reqStartTime;
    }
    
    public String getReqStartCount(){
        return reqStartCount;
    }
    
    public String getReqEndTime(){
        return reqEndTime;
    }
    
    public String getReqEndCount(){
        return reqEndCount;
    }
    
    public String getUUID(){
        return uuid;
    }
}
