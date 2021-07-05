package gr.gunet.accesslogtoscim;

import gr.gunet.accesslogtoscim.auditdb.AuditDB;
import gr.gunet.accesslogtoscim.idserver.AccesslogEntry;
import gr.gunet.accesslogtoscim.auditdb.SendAgainEntity;
import gr.gunet.accesslogtoscim.ldap.LdapConnection;
import gr.gunet.accesslogtoscim.idserver.ScimServer;
import java.util.Collection;
import java.util.List;
import org.ldaptive.LdapEntry;
import java.util.Collections;
import java.util.HashSet;

public class Main {
    public static void main(String[] args){
        int i = 0;
        HashSet<String> identityServerAcronyms = new HashSet();
        while(i < args.length){
            if(args[i].equals("--identity-servers")){
                i++;
                if(i < args.length){
                    String[] isAcronyms = args[i].split(",");
                    Collections.addAll(identityServerAcronyms, isAcronyms);
                }else{
                    System.err.println("--identity-servers option encountered without arguements");
                    System.exit(1);
                }
            }
            i++;
        }
        
        if(identityServerAcronyms.isEmpty()){
            System.err.println("at least one identity server must be specified using the --identity-servers option");
            System.exit(1);
        }
        
        try{
            resendRequests(identityServerAcronyms);
            LdapConnection ldap = new LdapConnection("accesslog.properties");
            Collection<LdapEntry> accesslogEntries = ldap.search("reqDN=schGrAcPersonID=4O2W89P40RNZ8XIXATNNFZ,ou=People,dc=aueb,dc=gr");
            for(LdapEntry ldapEntry : accesslogEntries){
                sendNewRequest(identityServerAcronyms, new AccesslogEntry(ldapEntry));
            }
        }catch(Exception e){
            AuditDB.close();
            e.printStackTrace(System.err);
        }
        
        AuditDB.close();
    }
    
    public static void resendRequests(HashSet<String> serverAcronyms) throws Exception{
        List<SendAgainEntity> sendAgain = AuditDB.getInstance().select("SELECT sa FROM SendAgainEntity sa",SendAgainEntity.class);
        for(SendAgainEntity sa : sendAgain){
            if(serverAcronyms.contains(sa.getScimServer())){
                ScimServer identityServer = ScimServer.getInstance(sa.getScimServer());
                identityServer.resendRequest(sa);
            }
        }
    }
    
    public static void sendNewRequest(HashSet<String> serverAcronyms,AccesslogEntry accesslogEntry) throws Exception{
        for(String serverAcronym : serverAcronyms){
            ScimServer identityServer = ScimServer.getInstance(serverAcronym);
            identityServer.sendNewRequest(accesslogEntry);
        }
    }
}
