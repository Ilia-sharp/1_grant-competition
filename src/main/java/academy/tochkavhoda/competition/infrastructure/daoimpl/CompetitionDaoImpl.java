package academy.tochkavhoda.competition.infrastructure.daoimpl;

import academy.tochkavhoda.competition.application.dao.CompetitionDao;
import academy.tochkavhoda.competition.domain.Database;
import academy.tochkavhoda.competition.domain.models.CompetitionSummary;
import academy.tochkavhoda.competition.domain.models.GrantApplication;

import java.util.List;
import java.util.Map;

public class CompetitionDaoImpl implements CompetitionDao {
    private final Database database = Database.getInstance();

    @Override
    public Map<GrantApplication, List<Integer>> getAllApplicationsWithRatings() {
        return database.getAllApplicationsWithRatings();
    }

    @Override
    public boolean isFinished() {
        return database.isCompetitionFinished();
    }

    @Override
    public void saveSummary(CompetitionSummary summary) {
        database.saveCompetitionSummary(summary);
    }

    @Override
    public CompetitionSummary getSummary() {
        return database.getCompetitionSummary();
    }
}
