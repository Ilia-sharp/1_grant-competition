package academy.tochkavhoda.competition.application.dao;

import academy.tochkavhoda.competition.domain.models.GrantApplication;
import academy.tochkavhoda.competition.domain.models.Participant;

import java.util.Collection;
import java.util.List;

/** Доступ к данным заявок. Реализация находится в слое infrastructure. */
public interface ApplicationDao {
    List<Integer> insert(List<GrantApplication> applications);

    GrantApplication getById(int id);

    List<GrantApplication> getByParticipant(Participant participant);

    List<GrantApplication> getByDirections(Collection<String> directions);

    void delete(GrantApplication application);
}
