package academy.tochkavhoda.competition.application.dao;

import academy.tochkavhoda.competition.domain.models.Expert;
import academy.tochkavhoda.competition.domain.models.GrantApplication;

import java.util.Map;

/** Доступ к данным оценок. Реализация находится в слое infrastructure. */
public interface RatingDao {
    Integer get(Expert expert, GrantApplication application);

    void set(Expert expert, GrantApplication application, int rating);

    void delete(Expert expert, GrantApplication application);

    Map<GrantApplication, Integer> getByExpert(Expert expert);
}
