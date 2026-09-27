package adventureconstructor.dao;

import adventureconstructor.dao.utils.EntityDAO;
import adventureconstructor.models.db.Item;
import adventureconstructor.utils.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class ItemsDAO extends EntityDAO<Item> {
    public ItemsDAO(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory);
    }

    @Override
    protected CriteriaQuery<Item> getSelectQuery(CriteriaBuilder builder, Long id) {
        CriteriaQuery<Item> preparedQuery = null;

        Logger.getInstance().info("Get item");

        preparedQuery = builder.createQuery(Item.class);
        Root<Item> root = preparedQuery.from(Item.class);
        preparedQuery = preparedQuery.select(root);
        preparedQuery = preparedQuery.where(builder.equal(root.get("id"), id));

        return preparedQuery;
    }

    @Override
    protected CriteriaQuery<Item> getSelectAllQuery(CriteriaBuilder builder) {
        CriteriaQuery<Item> preparedQuery = null;

        Logger.getInstance().info("Get all items");

        preparedQuery = builder.createQuery(Item.class);
        Root<Item> root = preparedQuery.from(Item.class);
        preparedQuery = preparedQuery.select(root);

        return preparedQuery;
    }

    @Override
    protected Item searchEntity(Item entity, EntityManager manager) {
        Logger.getInstance().info("Search item");

        return entity == null || entity.getId() == null ? null : manager.find(Item.class, entity.getId());
    }
}
