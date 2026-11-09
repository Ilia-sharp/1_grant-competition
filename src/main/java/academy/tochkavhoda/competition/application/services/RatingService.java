package academy.tochkavhoda.competition.application.services;

import academy.tochkavhoda.competition.application.commands.RateApplicationCommand;
import academy.tochkavhoda.competition.application.dao.ApplicationDao;
import academy.tochkavhoda.competition.application.dao.CompetitionDao;
import academy.tochkavhoda.competition.application.dao.RatingDao;
import academy.tochkavhoda.competition.application.dao.UserDao;
import academy.tochkavhoda.competition.application.exceptions.ApplicationException;
import academy.tochkavhoda.competition.application.exceptions.ErrorCode;
import academy.tochkavhoda.competition.application.results.ApplicationResult;
import academy.tochkavhoda.competition.application.results.RatedApplicationResult;
import academy.tochkavhoda.competition.domain.models.Expert;
import academy.tochkavhoda.competition.domain.models.GrantApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static academy.tochkavhoda.competition.application.services.ServiceUtils.checkCompetitionNotFinished;
import static academy.tochkavhoda.competition.application.services.ServiceUtils.getExpertByToken;

/** Оценки заявок экспертами. */
public class RatingService {
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private final UserDao userDao;
    private final ApplicationDao applicationDao;
    private final RatingDao ratingDao;
    private final CompetitionDao competitionDao;

    public RatingService(UserDao userDao, ApplicationDao applicationDao, RatingDao ratingDao,
                         CompetitionDao competitionDao) {
        this.userDao = userDao;
        this.applicationDao = applicationDao;
        this.ratingDao = ratingDao;
        this.competitionDao = competitionDao;
    }

    /** Эксперт ставит оценку заявке, которую ещё не оценивал. */
    public void rateApplication(String token, RateApplicationCommand command) {
        Expert expert = getExpertByToken(userDao, token);
        checkCompetitionNotFinished(competitionDao);
        validate(command);
        GrantApplication application = getApplicationForExpert(expert, command.applicationId);
        if (ratingDao.get(expert, application) != null) {
            throw new ApplicationException(ErrorCode.ALREADY_RATED, application.getId());
        }
        ratingDao.set(expert, application, command.rating);
    }

    /** Эксперт меняет ранее поставленную оценку. */
    public void changeRating(String token, RateApplicationCommand command) {
        Expert expert = getExpertByToken(userDao, token);
        checkCompetitionNotFinished(competitionDao);
        validate(command);
        GrantApplication application = getApplicationForExpert(expert, command.applicationId);
        if (ratingDao.get(expert, application) == null) {
            throw new ApplicationException(ErrorCode.RATING_NOT_FOUND, application.getId());
        }
        ratingDao.set(expert, application, command.rating);
    }

    /** Эксперт удаляет свою оценку. */
    public void deleteRating(String token, Integer applicationId) {
        Expert expert = getExpertByToken(userDao, token);
        checkCompetitionNotFinished(competitionDao);
        if (applicationId == null) {
            throw new ApplicationException(ErrorCode.WRONG_APPLICATION_ID);
        }
        GrantApplication application = applicationDao.getById(applicationId);
        if (application == null || ratingDao.get(expert, application) == null) {
            throw new ApplicationException(ErrorCode.RATING_NOT_FOUND, applicationId);
        }
        ratingDao.delete(expert, application);
    }

    /** Все заявки, которым эксперт поставил оценку, вместе с этой оценкой. */
    public List<RatedApplicationResult> getRatedApplications(String token) {
        Expert expert = getExpertByToken(userDao, token);
        List<RatedApplicationResult> results = new ArrayList<>();
        for (Map.Entry<GrantApplication, Integer> entry : ratingDao.getByExpert(expert).entrySet()) {
            results.add(new RatedApplicationResult(ApplicationResult.from(entry.getKey()), entry.getValue()));
        }
        return results;
    }

    private GrantApplication getApplicationForExpert(Expert expert, int applicationId) {
        GrantApplication application = applicationDao.getById(applicationId);
        if (application == null) {
            throw new ApplicationException(ErrorCode.APPLICATION_NOT_FOUND, applicationId);
        }
        // эксперт оценивает только заявки, у которых есть хотя бы одно его направление
        if (Collections.disjoint(expert.getDirections(), application.getDirections())) {
            throw new ApplicationException(ErrorCode.APPLICATION_OUT_OF_EXPERTISE, applicationId);
        }
        return application;
    }

    private static void validate(RateApplicationCommand command) {
        if (command.applicationId == null) {
            throw new ApplicationException(ErrorCode.WRONG_APPLICATION_ID);
        }
        if (command.rating == null || command.rating < MIN_RATING || command.rating > MAX_RATING) {
            throw new ApplicationException(ErrorCode.WRONG_RATING);
        }
    }
}
