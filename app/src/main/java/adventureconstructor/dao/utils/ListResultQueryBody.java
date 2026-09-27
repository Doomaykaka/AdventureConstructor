package adventureconstructor.dao.utils;

import java.util.List;
import javax.persistence.EntityManager;

public interface ListResultQueryBody<T> {
    public List<T> execute(EntityManager manager);
}
