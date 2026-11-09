package academy.tochkavhoda.competition.application.dao;

import academy.tochkavhoda.competition.domain.models.Participant;
import academy.tochkavhoda.competition.domain.models.User;

import java.util.List;

/** Доступ к данным пользователей. Реализация находится в слое infrastructure. */
public interface UserDao {
    void insert(User user);

    User getByLogin(String login);

    boolean isCompanyRegistered(String companyName);

    User getByToken(String token);

    String login(User user);

    void logout(String token);

    void delete(User user);

    List<Participant> getAllParticipants();
}
