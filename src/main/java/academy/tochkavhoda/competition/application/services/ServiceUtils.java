package academy.tochkavhoda.competition.application.services;

import academy.tochkavhoda.competition.application.dao.CompetitionDao;
import academy.tochkavhoda.competition.application.dao.UserDao;
import academy.tochkavhoda.competition.application.exceptions.ApplicationException;
import academy.tochkavhoda.competition.application.exceptions.ErrorCode;
import academy.tochkavhoda.competition.application.results.ApplicationResult;
import academy.tochkavhoda.competition.domain.models.Expert;
import academy.tochkavhoda.competition.domain.models.GrantApplication;
import academy.tochkavhoda.competition.domain.models.Participant;
import academy.tochkavhoda.competition.domain.models.User;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Общие для сервисов проверки: токен, роль пользователя, направления, состояние конкурса. */
final class ServiceUtils {
    private ServiceUtils() {
    }

    static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /** Приводит направления к единому виду (без пробелов по краям, в нижнем регистре), убирает дубли. */
    static Set<String> normalizeDirections(List<String> directions) {
        if (directions == null || directions.isEmpty()) {
            throw new ApplicationException(ErrorCode.WRONG_DIRECTIONS);
        }
        Set<String> result = new LinkedHashSet<>();
        for (String direction : directions) {
            if (isBlank(direction)) {
                throw new ApplicationException(ErrorCode.WRONG_DIRECTIONS);
            }
            result.add(direction.trim().toLowerCase(Locale.ROOT));
        }
        return result;
    }

    static User getUserByToken(UserDao userDao, String token) {
        if (isBlank(token)) {
            throw new ApplicationException(ErrorCode.WRONG_TOKEN);
        }
        User user = userDao.getByToken(token);
        if (user == null) {
            throw new ApplicationException(ErrorCode.WRONG_TOKEN);
        }
        return user;
    }

    static Participant getParticipantByToken(UserDao userDao, String token) {
        User user = getUserByToken(userDao, token);
        if (!(user instanceof Participant)) {
            throw new ApplicationException(ErrorCode.NOT_PARTICIPANT);
        }
        return (Participant) user;
    }

    static Expert getExpertByToken(UserDao userDao, String token) {
        User user = getUserByToken(userDao, token);
        if (!(user instanceof Expert)) {
            throw new ApplicationException(ErrorCode.NOT_EXPERT);
        }
        return (Expert) user;
    }

    /** После подведения итогов подача, отмена и оценка заявок запрещены. */
    static void checkCompetitionNotFinished(CompetitionDao competitionDao) {
        if (competitionDao.isFinished()) {
            throw new ApplicationException(ErrorCode.COMPETITION_FINISHED);
        }
    }

    static List<ApplicationResult> toResults(List<GrantApplication> applications) {
        List<ApplicationResult> results = new ArrayList<>();
        for (GrantApplication application : applications) {
            results.add(ApplicationResult.from(application));
        }
        return results;
    }
}
