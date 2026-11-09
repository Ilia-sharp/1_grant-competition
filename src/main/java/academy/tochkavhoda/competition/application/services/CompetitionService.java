package academy.tochkavhoda.competition.application.services;

import academy.tochkavhoda.competition.application.commands.SummarizeCommand;
import academy.tochkavhoda.competition.application.dao.CompetitionDao;
import academy.tochkavhoda.competition.application.dao.UserDao;
import academy.tochkavhoda.competition.application.exceptions.ApplicationException;
import academy.tochkavhoda.competition.application.exceptions.ErrorCode;
import academy.tochkavhoda.competition.application.results.CompetitionSummaryResult;
import academy.tochkavhoda.competition.domain.models.CompetitionSummary;
import academy.tochkavhoda.competition.domain.models.Grant;
import academy.tochkavhoda.competition.domain.models.GrantApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static academy.tochkavhoda.competition.application.services.ServiceUtils.checkCompetitionNotFinished;
import static academy.tochkavhoda.competition.application.services.ServiceUtils.getUserByToken;

/** Подведение итогов конкурса и просмотр итогов. */
public class CompetitionService {
    private static final double MIN_THRESHOLD = 0;
    private static final double MAX_THRESHOLD = 5;

    private final UserDao userDao;
    private final CompetitionDao competitionDao;
    private final Random random = new Random();

    public CompetitionService(UserDao userDao, CompetitionDao competitionDao) {
        this.userDao = userDao;
        this.competitionDao = competitionDao;
    }

    /**
     * Подведение итогов (момент X). После него подача, отмена и оценка заявок запрещены.
     * Алгоритм:
     * 1) заявки ранжируются по убыванию средней оценки (заявка без оценок имеет среднюю 0),
     *    при равной средней - по возрастанию запрашиваемой суммы, при равных суммах - случайно;
     * 2) заявки просматриваются по порядку: если сумма заявки не превышает остатка фонда,
     *    грант выделяется и сумма вычитается из фонда, иначе заявка пропускается;
     * 3) как только встречается заявка со средней оценкой не больше порога - распределение прекращается.
     */
    public CompetitionSummaryResult summarize(String token, SummarizeCommand command) {
        getUserByToken(userDao, token);
        validate(command);
        checkCompetitionNotFinished(competitionDao);

        List<RankedApplication> ranking = rank(competitionDao.getAllApplicationsWithRatings());
        long remaining = command.fund;
        List<Grant> grants = new ArrayList<>();
        for (RankedApplication ranked : ranking) {
            if (ranked.averageRating <= command.threshold) {
                break;
            }
            long sum = ranked.application.getRequestedSum();
            if (sum <= remaining) {
                grants.add(new Grant(ranked.application, ranked.averageRating, sum));
                remaining -= sum;
            }
        }
        CompetitionSummary summary = new CompetitionSummary(command.fund, command.threshold, grants);
        competitionDao.saveSummary(summary);
        return CompetitionSummaryResult.from(summary);
    }

    /** Итоги конкурса доступны любому вошедшему пользователю после их подведения. */
    public CompetitionSummaryResult getResults(String token) {
        getUserByToken(userDao, token);
        CompetitionSummary summary = competitionDao.getSummary();
        if (summary == null) {
            throw new ApplicationException(ErrorCode.COMPETITION_NOT_FINISHED);
        }
        return CompetitionSummaryResult.from(summary);
    }

    private List<RankedApplication> rank(Map<GrantApplication, List<Integer>> applicationsWithRatings) {
        List<RankedApplication> ranking = new ArrayList<>();
        for (Map.Entry<GrantApplication, List<Integer>> entry : applicationsWithRatings.entrySet()) {
            ranking.add(new RankedApplication(entry.getKey(), average(entry.getValue())));
        }
        // перемешиваем, а затем используем устойчивую сортировку:
        // заявки с одинаковыми оценкой и суммой останутся в случайном порядке
        Collections.shuffle(ranking, random);
        ranking.sort(Comparator
                .comparingDouble((RankedApplication r) -> r.averageRating).reversed()
                .thenComparingLong(r -> r.application.getRequestedSum()));
        return ranking;
    }

    private static double average(List<Integer> ratings) {
        if (ratings.isEmpty()) {
            return 0;
        }
        long total = 0;
        for (int rating : ratings) {
            total += rating;
        }
        return (double) total / ratings.size();
    }

    private static void validate(SummarizeCommand command) {
        if (command.fund == null || command.fund <= 0) {
            throw new ApplicationException(ErrorCode.WRONG_FUND);
        }
        if (command.threshold == null || command.threshold.isNaN()
                || command.threshold < MIN_THRESHOLD || command.threshold > MAX_THRESHOLD) {
            throw new ApplicationException(ErrorCode.WRONG_THRESHOLD);
        }
    }

    /** Заявка и её средняя оценка - нужны только для ранжирования. */
    private static final class RankedApplication {
        private final GrantApplication application;
        private final double averageRating;

        private RankedApplication(GrantApplication application, double averageRating) {
            this.application = application;
            this.averageRating = averageRating;
        }
    }
}
