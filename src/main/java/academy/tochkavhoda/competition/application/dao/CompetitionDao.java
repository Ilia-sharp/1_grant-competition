package academy.tochkavhoda.competition.application.dao;

import academy.tochkavhoda.competition.domain.models.CompetitionSummary;
import academy.tochkavhoda.competition.domain.models.GrantApplication;

import java.util.List;
import java.util.Map;

/** Доступ к данным итогов конкурса. Реализация находится в слое infrastructure. */
public interface CompetitionDao {
    Map<GrantApplication, List<Integer>> getAllApplicationsWithRatings();

    boolean isFinished();

    void saveSummary(CompetitionSummary summary);

    CompetitionSummary getSummary();
}
