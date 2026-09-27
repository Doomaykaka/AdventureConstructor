package adventureconstructor.dao.utils;

import javax.persistence.EntityManager;

public interface SingleResultQueryBody<T> {
    public T execute(EntityManager manager);
}
