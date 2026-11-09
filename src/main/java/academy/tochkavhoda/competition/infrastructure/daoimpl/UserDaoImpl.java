package academy.tochkavhoda.competition.infrastructure.daoimpl;

import academy.tochkavhoda.competition.application.dao.UserDao;
import academy.tochkavhoda.competition.domain.Database;
import academy.tochkavhoda.competition.domain.models.Participant;
import academy.tochkavhoda.competition.domain.models.User;

import java.util.List;

public class UserDaoImpl implements UserDao {
    private final Database database = Database.getInstance();

    @Override
    public void insert(User user) {
        database.insertUser(user);
    }

    @Override
    public User getByLogin(String login) {
        return database.getUserByLogin(login);
    }

    @Override
    public boolean isCompanyRegistered(String companyName) {
        return database.isCompanyRegistered(companyName);
    }

    @Override
    public User getByToken(String token) {
        return database.getUserByToken(token);
    }

    @Override
    public String login(User user) {
        return database.login(user);
    }

    @Override
    public void logout(String token) {
        database.logout(token);
    }

    @Override
    public void delete(User user) {
        database.deleteUser(user);
    }

    @Override
    public List<Participant> getAllParticipants() {
        return database.getParticipants();
    }
}
