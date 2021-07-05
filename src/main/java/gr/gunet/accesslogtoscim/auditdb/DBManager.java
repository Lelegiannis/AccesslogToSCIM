package gr.gunet.accesslogtoscim.auditdb;


import gr.gunet.accesslogtoscim.tools.PropertyReader;
import java.util.List;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import org.hibernate.Session;

public class DBManager {
    private final EntityManagerFactory emFactory;
    private EntityManager entityManager;
    private EntityTransaction transaction;
    private boolean active;
    private boolean activeDueToTransaction;
    
    private String connector;
    private PropertyReader propReader;
    
    public DBManager(String connector,String propFileName) throws Exception{
        this(connector,new PropertyReader(propFileName));
    }
    
    public DBManager(String connector,PropertyReader propReader){
        emFactory = Persistence.createEntityManagerFactory(connector,propReader.getPropertiesObject());
        entityManager = null;
        transaction = null;
        active = false;
        activeDueToTransaction = false;
        this.connector = connector;
        this.propReader = propReader;
    }
    
    public DBManager(DBManager dbmanager) throws Exception{
        this(dbmanager.connector,dbmanager.propReader);
    }
    
    public <T> Stream<T> streamedSelect(String sql,Class<T> entityClass) throws Exception{
        if(!active){
            throw new Exception("streamedSelect can be used only with DBManager in active state");
        }
        
        Session session = entityManager.unwrap(Session.class);
        return session.createQuery(sql, entityClass).stream();
    }
    
    public <T> List<T> select(String sql,Class<T> entityClass) throws Exception{
        return select(sql,entityClass,null,null);
    }
    
    public <T> List<T> select(String sql,Class<T> entityClass,Integer limit) throws Exception{
        return select(sql,entityClass,null,limit);
    }
    
    public <T> List<T> select(String sql,Class<T> entityClass,Integer page,Integer pageSize) throws Exception{
        if(!active){
            entityManager = emFactory.createEntityManager();
        }
        
        TypedQuery<T> query = entityManager.createQuery(sql,entityClass);
        if(pageSize != null){
            query.setMaxResults(pageSize);
            if(page != null){
                query.setFirstResult((page-1)*pageSize);
            }
        }
        List<T> results = query.getResultList();
        
        if(!active){
            entityManager.close();
        }
        
        return results;
    }
    
    public void beginTransaction() throws Exception{
        if(!active){
            activate();
            activeDueToTransaction = true;
        }
        
        if(transaction == null){
            try{
                transaction = entityManager.getTransaction();
                transaction.begin();
            }catch(Exception e){
                if(activeDueToTransaction){
                    inactivate();
                }
                throw e;
            }
        }else{
            throw new Exception("A transaction is already active");
        }
    }
    
    public void commitTransaction() throws Exception{
        if(transaction != null){
            transaction.commit();
            transaction = null;
            if(activeDueToTransaction){
                inactivate();
            }
        }
    }
    
    public void rollbackTransaction() throws Exception{
        if(transaction != null){
            transaction.rollback();
            transaction = null;
            if(activeDueToTransaction){
                inactivate();
            }
        }
    }
    
    public void update(Object entity) throws Exception{
        if(!active){
            entityManager = emFactory.createEntityManager();
        }
        try{
            if(transaction == null){
                EntityTransaction localTransaction = entityManager.getTransaction();
                localTransaction.begin();
                entityManager.merge(entity);
                localTransaction.commit();
            }else{
                entityManager.merge(entity);
            }
        }catch(Exception e){
            if(!active){
                entityManager.close();
            }
            throw e;
        }
        if(!active){
            entityManager.close();
        }
    }
    
    public void insert(Object entity) throws Exception{
        if(!active){
            entityManager = emFactory.createEntityManager();
        }
        try{
            if(transaction == null){
                EntityTransaction localTransaction = entityManager.getTransaction();
                localTransaction.begin();
                entityManager.persist(entity);
                localTransaction.commit();
            }else{
                entityManager.persist(entity);
            }
        }catch(Exception e){
            if(!active){
                entityManager.close();
            }
            throw e;
        }
        if(!active){
            entityManager.close();
        }
    }
    
    public void delete(Object entity) throws Exception{
        if(!active){
            entityManager = emFactory.createEntityManager();
        }
        try{
            if(transaction == null){
                EntityTransaction localTransaction = entityManager.getTransaction();
                localTransaction.begin();
                entityManager.remove(entity);
                localTransaction.commit();
            }else{
                entityManager.remove(entity);
            }
        }catch(Exception e){
            if(!active){
                entityManager.close();
            }
            throw e;
        }
        if(!active){
            entityManager.close();
        }
    }
    
    public void activate(){
        entityManager = emFactory.createEntityManager();
        active = true;
    }
    
    public void inactivate(){
        if(entityManager != null){
            entityManager.close();
        }
        active = false;
        activeDueToTransaction = false;
    }
    
    public void close(){
        if(active){
            inactivate();
        }
        emFactory.close();
    }
}
