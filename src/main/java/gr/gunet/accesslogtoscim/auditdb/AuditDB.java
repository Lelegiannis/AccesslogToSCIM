package gr.gunet.accesslogtoscim.auditdb;

public class AuditDB {
    private static DBManager INSTANCE = null;
    
    public static DBManager getInstance() throws Exception{
        if(INSTANCE == null){
            INSTANCE = new DBManager("mysql","db.properties");
        }
        return INSTANCE;
    }
    
    public static void close(){
        if(INSTANCE == null){
            return;
        }else{
            INSTANCE.close();
        }
    }
}
