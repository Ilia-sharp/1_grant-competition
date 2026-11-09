package academy.tochkavhoda.competition.application.services;

import academy.tochkavhoda.competition.application.commands.AddApplicationCommand;
import academy.tochkavhoda.competition.application.dao.ApplicationDao;
import academy.tochkavhoda.competition.application.dao.CompetitionDao;
import academy.tochkavhoda.competition.application.dao.UserDao;
import academy.tochkavhoda.competition.application.exceptions.ApplicationException;
import academy.tochkavhoda.competition.application.exceptions.ErrorCode;
import academy.tochkavhoda.competition.application.results.AddApplicationResult;
import academy.tochkavhoda.competition.application.results.AddApplicationsResult;
import academy.tochkavhoda.competition.application.results.ApplicationResult;
import academy.tochkavhoda.competition.domain.models.Expert;
import academy.tochkavhoda.competition.domain.models.GrantApplication;
import academy.tochkavhoda.competition.domain.models.Participant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static academy.tochkavhoda.competition.application.services.ServiceUtils.checkCompetitionNotFinished;
import static academy.tochkavhoda.competition.application.services.ServiceUtils.getExpertByToken;
import static academy.tochkavhoda.competition.application.services.ServiceUtils.getParticipantByToken;
import static academy.tochkavhoda.competition.application.services.ServiceUtils.isBlank;
import static academy.tochkavhoda.competition.application.services.ServiceUtils.normalizeDirections;
import static academy.tochkavhoda.competition.application.services.ServiceUtils.toResults;

/** Заявки на гранты: подача, отмена, просмотр. */
public class ApplicationService {
    private static final int MAX_TITLE_LENGTH = 200;

    private final UserDao userDao;
    private final ApplicationDao applicationDao;
    private final CompetitionDao competitionDao;

    public ApplicationService(UserDao userDao, ApplicationDao applicationDao, CompetitionDao competitionDao) {
        this.userDao = userDao;
        this.applicationDao = applicationDao;
        this.competitionDao = competitionDao;
    }

    public AddApplicationResult addApplication(String token, AddApplicationCommand command) {
        Participant participant = getParticipantByToken(userDao, token);
        checkCompetitionNotFinished(competitionDao);
        GrantApplication application = createApplication(participant, command);
        List<Integer> ids = applicationDao.insert(Collections.singletonList(application));
        return new AddApplicationResult(ids.get(0));
    }

    /** Добавление нескольких заявок. Если хотя бы одна некорректна - не добавляется ни одна. */
    public AddApplicationsResult addApplications(String token, List<AddApplicationCommand> commands) {
        Participant participant = getParticipantByToken(userDao, token);
        checkCompetitionNotFinished(competitionDao);
        if (commands == null || commands.isEmpty()) {
            throw new ApplicationException(ErrorCode.EMPTY_APPLICATION_LIST);
        }
        List<GrantApplication> applications = new ArrayList<>();
        for (AddApplicationCommand command : commands) {
            applications.add(createApplication(participant, command));
        }
        return new AddApplicationsResult(applicationDao.insert(applications));
    }

    /** Участник отменяет свою заявку (изменять заявку нельзя). */
    public void removeApplication(String token, Integer applicationId) {
        Participant participant = getParticipantByToken(userDao, token);
        checkCompetitionNotFinished(competitionDao);
        if (applicationId == null) {
            throw new ApplicationException(ErrorCode.WRONG_APPLICATION_ID);
        }
        GrantApplication application = applicationDao.getById(applicationId);
        if (application == null) {
            throw new ApplicationException(ErrorCode.APPLICATION_NOT_FOUND, applicationId);
        }
        if (!application.getParticipant().equals(participant)) {
            throw new ApplicationException(ErrorCode.NOT_APPLICATION_OWNER, applicationId);
        }
        applicationDao.delete(application);
    }

    public List<ApplicationResult> getMyApplications(String token) {
        Participant participant = getParticipantByToken(userDao, token);
        return toResults(applicationDao.getByParticipant(participant));
    }

    /**
     * Заявки по направлению или набору направлений эксперта.
     * Если направления не указаны - по всем направлениям эксперта.
     * Нельзя запрашивать направления, по которым пользователь не является экспертом.
     */
    public List<ApplicationResult> getApplicationsByDirections(String token, List<String> directions) {
        Expert expert = getExpertByToken(userDao, token);
        Set<String> selected;
        if (directions == null || directions.isEmpty()) {
            selected = expert.getDirections();
        } else {
            selected = normalizeDirections(directions);
            for (String direction : selected) {
                if (!expert.getDirections().contains(direction)) {
                    throw new ApplicationException(ErrorCode.DIRECTION_NOT_EXPERT, direction);
                }
            }
        }
        return toResults(applicationDao.getByDirections(selected));
    }

    private static GrantApplication createApplication(Participant participant, AddApplicationCommand command) {
        validate(command);
        return new GrantApplication(participant, command.title.trim(), command.description.trim(),
                normalizeDirections(command.directions), command.requestedSum);
    }

    private static void validate(AddApplicationCommand command) {
        if (command == null) {
            throw new ApplicationException(ErrorCode.EMPTY_APPLICATION);
        }
        if (isBlank(command.title) || command.title.trim().length() > MAX_TITLE_LENGTH) {
            throw new ApplicationException(ErrorCode.WRONG_TITLE);
        }
        if (isBlank(command.description)) {
            throw new ApplicationException(ErrorCode.WRONG_DESCRIPTION);
        }
        normalizeDirections(command.directions);
        if (command.requestedSum == null || command.requestedSum <= 0) {
            throw new ApplicationException(ErrorCode.WRONG_REQUESTED_SUM);
        }
    }
}
