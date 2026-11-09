package academy.tochkavhoda.competition.infrastructure.daoimpl;

import academy.tochkavhoda.competition.application.dao.RatingDao;
import academy.tochkavhoda.competition.domain.Database;
import academy.tochkavhoda.competition.domain.models.Expert;
import academy.tochkavhoda.competition.domain.models.GrantApplication;

import java.util.Map;

public class RatingDaoImpl implements RatingDao {
    private final Database database = Database.getInstance();

    @Override
    public Integer get(Expert expert, GrantApplication application) {
        return database.getRating(expert, application);
    }

    @Override
    public void set(Expert expert, GrantApplication application, int rating) {
        database.setRating(expert, application, rating);
    }

    @Override
    public void delete(Expert expert, GrantApplication application) {
        database.deleteRating(expert, application);
    }

    @Override
    public Map<GrantApplication, Integer> getByExpert(Expert expert) {
        return database.getRatingsByExpert(expert);
    }
}
