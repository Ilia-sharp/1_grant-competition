package academy.tochkavhoda.competition.infrastructure.daoimpl;

import academy.tochkavhoda.competition.application.dao.DebugDao;
import academy.tochkavhoda.competition.domain.Database;

public class DebugDaoImpl implements DebugDao {
    private final Database database = Database.getInstance();

    @Override
    public void clear() {
        database.clear();
    }
}
