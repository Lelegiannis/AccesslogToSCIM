package gr.gunet.accesslogtoscim.ldap;

import gr.gunet.accesslogtoscim.tools.PropertyReader;
import java.util.Collection;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.SortBehavior;

public class LdapConnection {
    private final ConnectionFactory cf;
    
    protected final String bindDN;
    protected final String baseDN;
    
    public LdapConnection(ConnectionFactory cf,String bindDN,String baseDN){
        this.cf = cf;

        this.bindDN = bindDN;
        this.baseDN = baseDN;
    }
    
    public LdapConnection(String propFile) throws Exception{
        PropertyReader propReader = new PropertyReader(propFile);
        
        Credential password = new Credential(propReader.getProperty("password"));
        BindConnectionInitializer connectionInitializer = new BindConnectionInitializer(propReader.getProperty("bindDN"), password);
        ConnectionConfig connConf = new ConnectionConfig(propReader.getProperty("url"));
        connConf.setConnectionInitializer(connectionInitializer);
        this.cf = new DefaultConnectionFactory(connConf);
        this.bindDN = propReader.getProperty("bindDN");
        this.baseDN = propReader.getProperty("baseDN");
    }
    
    protected Connection openConnection() throws LdapException{
        Connection conn = cf.getConnection();
        conn.open();
        return conn;
    }
    
    protected void closeConnection(Connection conn){
        if(conn != null){
            conn.close();
        }
    }
    
    public String getBindDN(){
        return bindDN;
    }
    
    public Collection<LdapEntry> search(String filter) throws LdapException,Exception{
        LdapSearchFilter searchFilter = new LdapSearchFilter(filter,baseDN);
        return search(searchFilter);
    }
    
    public Collection<LdapEntry> search(LdapSearchFilter searchFilter) throws LdapException,Exception{
        Connection conn = openConnection();
        
        SearchOperation search = new SearchOperation(conn);
        SearchRequest req = new SearchRequest(searchFilter.getDN(),searchFilter.toString());
        req.setSortBehavior(SortBehavior.ORDERED);
        SearchResult result = search.execute(req).getResult();
        Collection<LdapEntry> entries = result.getEntries();
        
        closeConnection(conn);
        
        return entries;
    }
}
