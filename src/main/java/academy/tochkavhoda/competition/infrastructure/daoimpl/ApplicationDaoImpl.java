package academy.tochkavhoda.competition.infrastructure.daoimpl;

import academy.tochkavhoda.competition.application.dao.ApplicationDao;
import academy.tochkavhoda.competition.domain.Database;
import academy.tochkavhoda.competition.domain.models.GrantApplication;
import academy.tochkavhoda.competition.domain.models.Participant;

import java.util.Collection;
import java.util.List;

public class ApplicationDaoImpl implements ApplicationDao {
    private final Database database = Database.getInstance();

    @Override
    public List<Integer> insert(List<GrantApplication> applications) {
        return database.insertApplications(applications);
    }

    @Override
    public GrantApplication getById(int id) {
        return database.getApplicationById(id);
    }

    @Override
    public List<GrantApplication> getByParticipant(Participant participant) {
        return database.getApplicationsByParticipant(participant);
    }

    @Override
    public List<GrantApplication> getByDirections(Collection<String> directions) {
        return database.getApplicationsByDirections(directions);
    }

    @Override
    public void delete(GrantApplication application) {
        database.deleteApplication(application);
    }
}
