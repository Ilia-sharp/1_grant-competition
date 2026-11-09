package academy.tochkavhoda.competition;

import academy.tochkavhoda.competition.api.dto.requests.ApplicationIdRequest;
import academy.tochkavhoda.competition.api.dto.requests.RateApplicationRequest;
import academy.tochkavhoda.competition.api.dto.responses.CompetitionSummaryResponse;
import academy.tochkavhoda.competition.api.dto.responses.GrantResponse;
import academy.tochkavhoda.competition.api.dto.responses.ServerResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Итоги конкурса может подвести любой вошедший пользователь (отдельной роли администратора нет),
 * поэтому в тестах для этого используется токен любого участника или эксперта.
 */
public class TestCompetition extends TestBase {
    private static final String COMPETITION_FINISHED = "Конкурс завершён, операция невозможна";

    private static List<Integer> grantedIds(CompetitionSummaryResponse summary) {
        List<Integer> ids = new ArrayList<>();
        for (GrantResponse grant : summary.grants) {
            ids.add(grant.applicationId);
        }
        return ids;
    }

    private void rateAll(int applicationId, String[] experts, int... ratings) {
        for (int i = 0; i < ratings.length; i++) {
            assertSuccess(rate(experts[i], applicationId, ratings[i]));
        }
    }

    @Test
    public void testSummarizeScenario() {
        String p1 = participant("romashka", "Ромашка");
        String p2 = participant("vasilek", "Василёк");
        String[] experts = {expert("expert1", "it"), expert("expert2", "it"), expert("expert3", "it")};

        int a = addApplication(p1, "A", 100, "it");  // средняя 14/3
        int b = addApplication(p2, "B", 300, "it");  // средняя 5
        int c = addApplication(p1, "C", 250, "it");  // средняя 4
        int d = addApplication(p2, "D", 50, "it");   // средняя 3
        addApplication(p1, "E", 10, "it");           // без оценок - средняя 0
        rateAll(a, experts, 5, 5, 4);
        rateAll(b, experts, 5, 5, 5);
        rateAll(c, experts, 4, 4);
        rateAll(d, experts, 3, 3);

        CompetitionSummaryResponse summary = summarize(p1, 500, 3.0);
        // B (300) -> остаток 200; A (100) -> остаток 100; C (250) не помещается и пропускается;
        // D имеет среднюю 3.0, не превышающую порог, - распределение прекращается
        assertEquals(Arrays.asList(b, a), grantedIds(summary));
        assertEquals(500L, summary.fund);
        assertEquals(3.0, summary.threshold, 1e-9);
        assertEquals(400L, summary.distributedSum);
        assertEquals(100L, summary.remainingSum);
        assertEquals(5.0, summary.grants.get(0).averageRating, 1e-9);
        assertEquals(14.0 / 3, summary.grants.get(1).averageRating, 1e-9);

        GrantResponse grant = summary.grants.get(0);
        assertEquals("B", grant.title);
        assertEquals("Василёк", grant.companyName);
        assertEquals(300L, grant.grantSum);
    }

    @Test
    public void testLowerThresholdGivesMoreGrants() {
        String p = participant("romashka", "Ромашка");
        String[] experts = {expert("expert1", "it"), expert("expert2", "it")};
        int a = addApplication(p, "A", 300, "it");
        int b = addApplication(p, "B", 250, "it");
        int c = addApplication(p, "C", 50, "it");
        rateAll(a, experts, 5, 5);
        rateAll(b, experts, 4, 4);
        rateAll(c, experts, 3, 3);

        CompetitionSummaryResponse summary = summarize(p, 500, 2.5);
        assertEquals(Arrays.asList(a, c), grantedIds(summary));
        assertEquals(150L, summary.remainingSum);
    }

    @Test
    public void testEqualAverageRankedBySum() {
        String p = participant("romashka", "Ромашка");
        String[] experts = {expert("expert1", "it")};
        int expensive = addApplication(p, "Дорогая", 200, "it");
        int cheap = addApplication(p, "Дешёвая", 100, "it");
        rateAll(expensive, experts, 5);
        rateAll(cheap, experts, 5);

        CompetitionSummaryResponse summary = summarize(p, 250, 0);
        // при равной средней первой идёт заявка с меньшей суммой, дорогая уже не помещается
        assertEquals(Collections.singletonList(cheap), grantedIds(summary));
        assertEquals(150L, summary.remainingSum);
    }

    @Test
    public void testEqualAverageAndSumRankedRandomly() {
        String p = participant("romashka", "Ромашка");
        String[] experts = {expert("expert1", "it")};
        int first = addApplication(p, "Первая", 100, "it");
        int second = addApplication(p, "Вторая", 100, "it");
        rateAll(first, experts, 4);
        rateAll(second, experts, 4);

        CompetitionSummaryResponse summary = summarize(p, 150, 0);
        assertEquals(1, summary.grants.size());
        int winner = summary.grants.get(0).applicationId;
        assertTrue(winner == first || winner == second);
    }

    @Test
    public void testApplicationsWithoutRatingsGetNothing() {
        String p = participant("romashka", "Ромашка");
        addApplication(p, "A", 100, "it");
        CompetitionSummaryResponse summary = summarize(p, 1000, 0);
        assertTrue(summary.grants.isEmpty());
        assertEquals(1000L, summary.remainingSum);
    }

    @Test
    public void testExpertLeavingChangesAverage() {
        String p = participant("romashka", "Ромашка");
        String good = expert("expert1", "it");
        String bad = expert("expert2", "it");
        int a = addApplication(p, "A", 100, "it");
        rateAll(a, new String[]{good, bad}, 5, 1);   // средняя 3
        assertSuccess(server.leaveServer(bad));       // остаётся только оценка 5

        CompetitionSummaryResponse summary = summarize(p, 1000, 3);
        assertEquals(Collections.singletonList(a), grantedIds(summary));
    }

    @Test
    public void testSummarizeNeedsLoggedInUser() {
        String p = participant("romashka", "Ромашка");
        String wrongToken = "Неверный токен: пользователь не вошёл на сервер";
        assertError(trySummarize("bad-token", 100L, 3.0), wrongToken);
        assertError(trySummarize(null, 100L, 3.0), wrongToken);
        assertSuccess(server.logout(p));
        assertError(trySummarize(p, 100L, 3.0), wrongToken);
    }

    @Test
    public void testSummarizeWrongParameters() {
        String p = participant("romashka", "Ромашка");
        assertError(trySummarize(p, 0L, 3.0), "Общий фонд гранта должен быть положительным");
        assertError(trySummarize(p, null, 3.0), "Общий фонд гранта должен быть положительным");
        assertError(trySummarize(p, 100L, -1.0), "Пороговое значение оценки должно быть в диапазоне от 0 до 5");
        assertError(trySummarize(p, 100L, 5.5), "Пороговое значение оценки должно быть в диапазоне от 0 до 5");
        assertError(trySummarize(p, 100L, null), "Пороговое значение оценки должно быть в диапазоне от 0 до 5");
        assertError(server.summarize(p, "{"), "Некорректный JSON запроса");
    }

    @Test
    public void testNothingChangesAfterSummarize() {
        String p = participant("romashka", "Ромашка");
        String e = expert("expert1", "it");
        int a = addApplication(p, "A", 100, "it");
        int b = addApplication(p, "B", 100, "it");
        assertSuccess(rate(e, a, 5));
        ServerResponse summarizeResponse = trySummarize(p, 1000L, 3.0);
        assertSuccess(summarizeResponse);

        assertError(tryAddApplication(p, "C", 100L, "it"), COMPETITION_FINISHED);
        assertError(server.removeApplication(p, toJson(new ApplicationIdRequest(a))), COMPETITION_FINISHED);
        assertError(rate(e, b, 5), COMPETITION_FINISHED);
        assertError(server.changeRating(e, toJson(new RateApplicationRequest(a, 1))), COMPETITION_FINISHED);
        assertError(server.deleteRating(e, toJson(new ApplicationIdRequest(a))), COMPETITION_FINISHED);
        assertError(trySummarize(p, 1000L, 3.0), COMPETITION_FINISHED);

        // итоги доступны любому вошедшему пользователю и совпадают с результатом подведения
        assertEquals(summarizeResponse.getResponseData(), server.getResults(p).getResponseData());
        assertEquals(summarizeResponse.getResponseData(), server.getResults(e).getResponseData());
    }

    @Test
    public void testResultsBeforeSummarize() {
        String p = participant("romashka", "Ромашка");
        assertError(server.getResults(p), "Итоги конкурса ещё не подведены");
        assertError(server.getResults("bad-token"), "Неверный токен: пользователь не вошёл на сервер");
    }
}
