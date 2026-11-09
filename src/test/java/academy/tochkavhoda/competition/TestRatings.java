package academy.tochkavhoda.competition;

import academy.tochkavhoda.competition.api.dto.requests.ApplicationIdRequest;
import academy.tochkavhoda.competition.api.dto.requests.RateApplicationRequest;
import academy.tochkavhoda.competition.api.dto.responses.RatedApplicationResponse;
import academy.tochkavhoda.competition.api.dto.responses.RatedApplicationsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestRatings extends TestBase {
    private static final String WRONG_RATING = "Оценка должна быть целым числом от 1 до 5";

    private String participantToken;
    private String expertToken;
    private int first;
    private int second;
    private int biology;

    @BeforeEach
    public void prepare() {
        participantToken = participant("romashka", "ООО Ромашка");
        first = addApplication(participantToken, "Заявка 1", 100, "информатика");
        second = addApplication(participantToken, "Заявка 2", 200, "информатика", "физика");
        biology = addApplication(participantToken, "Заявка 3", 300, "биология");
        expertToken = expert("expert1", "информатика");
    }

    private List<RatedApplicationResponse> rated() {
        return getData(server.getRatedApplications(expertToken), RatedApplicationsResponse.class).applications;
    }

    @Test
    public void testRateApplications() {
        assertSuccess(rate(expertToken, second, 4));
        assertSuccess(rate(expertToken, first, 5));
        List<RatedApplicationResponse> rated = rated();
        assertEquals(2, rated.size());
        assertEquals(first, rated.get(0).application.id);
        assertEquals(5, rated.get(0).rating);
        assertEquals(second, rated.get(1).application.id);
        assertEquals(4, rated.get(1).rating);
    }

    @Test
    public void testCannotRateTwice() {
        assertSuccess(rate(expertToken, first, 3));
        assertError(rate(expertToken, first, 4),
                "Заявка с id " + first + " уже оценена вами, используйте изменение оценки");
        assertEquals(3, rated().get(0).rating);
    }

    @Test
    public void testChangeRating() {
        assertSuccess(rate(expertToken, first, 3));
        assertSuccess(server.changeRating(expertToken, toJson(new RateApplicationRequest(first, 1))));
        assertEquals(1, rated().get(0).rating);
        assertError(server.changeRating(expertToken, toJson(new RateApplicationRequest(second, 1))),
                "Вы не оценивали заявку с id " + second);
    }

    @Test
    public void testDeleteRating() {
        assertSuccess(rate(expertToken, first, 3));
        assertSuccess(server.deleteRating(expertToken, toJson(new ApplicationIdRequest(first))));
        assertTrue(rated().isEmpty());
        assertError(server.deleteRating(expertToken, toJson(new ApplicationIdRequest(first))),
                "Вы не оценивали заявку с id " + first);
        // после удаления оценку можно поставить заново
        assertSuccess(rate(expertToken, first, 2));
    }

    @Test
    public void testWrongRating() {
        assertError(rate(expertToken, first, 0), WRONG_RATING);
        assertError(rate(expertToken, first, 6), WRONG_RATING);
        assertError(rate(expertToken, first, null), WRONG_RATING);
        assertError(server.rateApplication(expertToken, "{\"applicationId\": " + first + ", \"rating\": 4.5}"),
                "Некорректный JSON запроса");
        assertError(rate(expertToken, null, 4), "Не указан id заявки");
        assertError(rate(expertToken, 999, 4), "Заявка с id 999 не найдена");
        assertTrue(rated().isEmpty());
    }

    @Test
    public void testCannotRateOutsideExpertise() {
        assertError(rate(expertToken, biology, 5), "Заявка с id " + biology + " не относится к вашим направлениям");
    }

    @Test
    public void testOnlyExpertCanRate() {
        assertError(rate(participantToken, first, 5), "Операция доступна только экспертам");
        assertError(server.getRatedApplications(participantToken), "Операция доступна только экспертам");
    }

    @Test
    public void testRatingsOfRemovedApplicationDisappear() {
        assertSuccess(rate(expertToken, first, 5));
        assertSuccess(rate(expertToken, second, 4));
        assertSuccess(server.removeApplication(participantToken, toJson(new ApplicationIdRequest(first))));
        List<RatedApplicationResponse> rated = rated();
        assertEquals(1, rated.size());
        assertEquals(second, rated.get(0).application.id);
    }
}
