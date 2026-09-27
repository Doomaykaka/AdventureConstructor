package adventureconstructor.dao.utils;

import adventureconstructor.utils.Logger;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

public abstract class EntityDAO<T> {
    protected EntityManagerFactory entityManagerFactory;

    public EntityDAO(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public T get(Long id) {
        T result = null;

        if (!queryIsReadyToCreate(id)) {
            return result;
        }

        result = executeSingleResultQuery((EntityManager manager) -> {
            CriteriaBuilder builder = manager.getCriteriaBuilder();

            CriteriaQuery<T> query = getSelectQuery(builder, id);
            TypedQuery<T> preparedQuery = manager.createQuery(query);

            return preparedQuery.getSingleResult();
        });

        return result;
    }

    public List<T> getAll() {
        List<T> result = null;

        if (!queryIsReadyToCreate()) {
            return result;
        }

        result = executeListResultQuery((EntityManager manager) -> {
            CriteriaBuilder builder = manager.getCriteriaBuilder();

            CriteriaQuery<T> query = getSelectAllQuery(builder);
            TypedQuery<T> preparedQuery = manager.createQuery(query);

            return preparedQuery.getResultList();
        });

        return result;
    }

    public boolean update(T entity) {
        boolean isUpdated = false;

        if (!queryIsReadyToCreate(entity)) {
            return isUpdated;
        }

        executeQuery((EntityManager manager) -> {
            manager.merge(entity);
        });

        isUpdated = true;

        return isUpdated;
    }

    public boolean remove(T entity) {
        boolean isRemoved = false;

        if (!queryIsReadyToCreate(entity)) {
            return isRemoved;
        }

        executeQuery((EntityManager manager) -> {
            T foundedEntity = searchEntity(entity, manager);

            manager.remove(foundedEntity);
        });

        isRemoved = true;

        return isRemoved;
    }

    public boolean create(T entity) {
        boolean isCreated = false;

        if (!queryIsReadyToCreate(entity)) {
            return isCreated;
        }

        executeQuery((EntityManager manager) -> {
            T foundedEntity = searchEntity(entity, manager);

            if (foundedEntity != null) {
                Logger.getInstance().warning("Cant create entity");
            } else {
                manager.persist(entity);
            }
        });

        isCreated = true;

        return isCreated;
    }

    protected <M> void executeQuery(QueryBody queryBody) {
        EntityManager manager = this.entityManagerFactory.createEntityManager();

        try {
            EntityTransaction transaction = manager.getTransaction();

            transaction.begin();

            queryBody.execute(manager);

            transaction.commit();
        } catch (PersistenceException e) {
            Logger.getInstance().warning("Cant execute query");
        }

        if (manager != null) {
            manager.close();
        }
    }

    protected <M> M executeSingleResultQuery(SingleResultQueryBody<M> queryBody) {
        M result = null;

        EntityManager manager = this.entityManagerFactory.createEntityManager();

        try {
            EntityTransaction transaction = manager.getTransaction();

            transaction.begin();

            result = queryBody.execute(manager);

            transaction.commit();
        } catch (PersistenceException e) {
            Logger.getInstance().warning("Cant execute single result query");
        }

        if (manager != null) {
            manager.close();
        }

        return result;
    }

    protected <M> List<M> executeListResultQuery(ListResultQueryBody<M> queryBody) {
        List<M> result = null;

        EntityManager manager = this.entityManagerFactory.createEntityManager();

        try {
            EntityTransaction transaction = manager.getTransaction();

            transaction.begin();

            result = queryBody.execute(manager);

            transaction.commit();
        } catch (PersistenceException e) {
            Logger.getInstance().warning("Cant execute list result query");
        }

        if (manager != null) {
            manager.close();
        }

        return result;
    }

    protected abstract CriteriaQuery<T> getSelectQuery(CriteriaBuilder builder, Long id);

    protected abstract CriteriaQuery<T> getSelectAllQuery(CriteriaBuilder builder);

    protected abstract T searchEntity(T entity, EntityManager manager);

    protected boolean queryIsReadyToCreate(T entity) {
        boolean isReady = false;

        if (entity == null) {
            Logger.getInstance().warning("Empty object");
            return isReady;
        }

        if (!queryIsReadyToCreate()) {
            Logger.getInstance().warning("Query must be redy to create");
            return isReady;
        }

        isReady = true;

        return isReady;
    }

    protected boolean queryIsReadyToCreate(Long id) {
        boolean isReady = false;

        if (id == null) {
            Logger.getInstance().warning("Empty identifier");
            return isReady;
        }

        if (!queryIsReadyToCreate()) {
            Logger.getInstance().warning("Query must be redy to create");
            return isReady;
        }

        isReady = true;

        return isReady;
    }

    protected boolean queryIsReadyToCreate() {
        boolean isReady = false;

        isReady = this.entityManagerFactory != null;

        if (!isReady) {
            Logger.getInstance().warning("Empty entity manager factory");
        }

        return isReady;
    }
}
