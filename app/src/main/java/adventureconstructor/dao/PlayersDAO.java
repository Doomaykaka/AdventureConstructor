package adventureconstructor.dao;

import adventureconstructor.dao.utils.EntityDAO;
import adventureconstructor.models.db.Player;
import adventureconstructor.utils.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class PlayersDAO extends EntityDAO<Player> {
    public PlayersDAO(EntityManagerFactory entityManagerFactory) {
        super(entityManagerFactory);
    }

    @Override
    protected CriteriaQuery<Player> getSelectQuery(CriteriaBuilder builder, Long id) {
        CriteriaQuery<Player> preparedQuery = null;

        Logger.getInstance().info("Get player");

        preparedQuery = builder.createQuery(Player.class);
        Root<Player> root = preparedQuery.from(Player.class);
        preparedQuery = preparedQuery.select(root);
        preparedQuery = preparedQuery.where(builder.equal(root.get("id"), id));

        return preparedQuery;
    }

    @Override
    protected CriteriaQuery<Player> getSelectAllQuery(CriteriaBuilder builder) {
        CriteriaQuery<Player> preparedQuery = null;

        Logger.getInstance().info("Get all players");

        preparedQuery = builder.createQuery(Player.class);
        Root<Player> root = preparedQuery.from(Player.class);
        preparedQuery = preparedQuery.select(root);

        return preparedQuery;
    }

    @Override
    protected Player searchEntity(Player entity, EntityManager manager) {
        Logger.getInstance().info("Search player");

        return entity == null || entity.getId() == null ? null : manager.find(Player.class, entity.getId());
    }
}
