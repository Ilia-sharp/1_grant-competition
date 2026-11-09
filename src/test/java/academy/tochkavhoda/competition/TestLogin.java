package academy.tochkavhoda.competition;

import academy.tochkavhoda.competition.api.dto.responses.ApplicationsResponse;
import academy.tochkavhoda.competition.api.dto.responses.RatedApplicationsResponse;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestLogin extends TestBase {
    private static final String WRONG_LOGIN_OR_PASSWORD = "Неверный логин или пароль";
    private static final String WRONG_TOKEN = "Неверный токен: пользователь не вошёл на сервер";

    @Test
    public void testLoginReturnsUniqueTokens() {
        assertSuccess(registerParticipant("romashka", "ООО Ромашка"));
        assertSuccess(registerExpert("expert1", "физика"));
        assertSuccess(registerExpert("expert2", "химия"));
        String token1 = loginAndGetToken("romashka", PASSWORD);
        String token2 = loginAndGetToken("expert1", PASSWORD);
        String token3 = loginAndGetToken("expert2", PASSWORD);
        assertNotEquals(token1, token2);
        assertNotEquals(token2, token3);
        assertNotEquals(token1, token3);
    }

    @Test
    public void testLoginWrongCredentials() {
        assertSuccess(registerParticipant("romashka", "ООО Ромашка"));
        assertError(login("romashka", "Wrong#123"), WRONG_LOGIN_OR_PASSWORD);
        assertError(login("unknown", PASSWORD), WRONG_LOGIN_OR_PASSWORD);
        assertError(login("", PASSWORD), WRONG_LOGIN_OR_PASSWORD);
        assertError(login("romashka", null), WRONG_LOGIN_OR_PASSWORD);
        assertError(server.login("{"), "Некорректный JSON запроса");
    }

    @Test
    public void testLogoutMakesTokenInvalid() {
        String token = participant("romashka", "ООО Ромашка");
        assertSuccess(server.getParticipants(token, null));
        assertSuccess(server.logout(token));
        assertError(server.getParticipants(token, null), WRONG_TOKEN);
        assertError(server.logout(token), WRONG_TOKEN);
    }

    @Test
    public void testLoginAgainGivesNewToken() {
        String oldToken = participant("romashka", "ООО Ромашка");
        assertSuccess(server.logout(oldToken));
        String newToken = loginAndGetToken("romashka", PASSWORD);
        assertNotEquals(oldToken, newToken);
        assertSuccess(server.getParticipants(newToken, null));
        assertError(server.getParticipants(oldToken, null), WRONG_TOKEN);
    }

    @Test
    public void testSecondLoginInvalidatesPreviousToken() {
        String firstToken = participant("romashka", "ООО Ромашка");
        String secondToken = loginAndGetToken("romashka", PASSWORD);
        assertError(server.getParticipants(firstToken, null), WRONG_TOKEN);
        assertSuccess(server.getParticipants(secondToken, null));
    }

    @Test
    public void testWrongToken() {
        assertError(server.getParticipants(null, null), WRONG_TOKEN);
        assertError(server.getParticipants("", null), WRONG_TOKEN);
        assertError(server.getParticipants("123e4567-e89b-12d3-a456-426614174000", null), WRONG_TOKEN);
    }

    @Test
    public void testParticipantLeavesServer() {
        String participantToken = participant("romashka", "ООО Ромашка");
        addApplication(participantToken, "Робот-пылесос", 1000, "информатика");
        String expertToken = expert("expert1", "информатика");
        assertEquals(1, getData(server.getApplicationsByDirections(expertToken, null),
                ApplicationsResponse.class).applications.size());

        assertSuccess(server.leaveServer(participantToken));
        assertError(server.getParticipants(participantToken, null), WRONG_TOKEN);
        assertError(login("romashka", PASSWORD), WRONG_LOGIN_OR_PASSWORD);
        // заявки ушедшего участника уничтожены
        assertTrue(getData(server.getApplicationsByDirections(expertToken, null),
                ApplicationsResponse.class).applications.isEmpty());
        // логин и название фирмы снова свободны
        String newToken = participant("romashka", "ООО Ромашка");
        assertEquals(Collections.emptyList(),
                getData(server.getMyApplications(newToken), ApplicationsResponse.class).applications);
    }

    @Test
    public void testExpertLeavesServer() {
        String participantToken = participant("romashka", "ООО Ромашка");
        int id = addApplication(participantToken, "Робот-пылесос", 1000, "информатика");
        String expertToken = expert("expert1", "информатика");
        assertSuccess(rate(expertToken, id, 5));

        assertSuccess(server.leaveServer(expertToken));
        assertError(server.getRatedApplications(expertToken), WRONG_TOKEN);
        // после повторной регистрации у эксперта нет прежних оценок
        String newExpertToken = expert("expert1", "информатика");
        assertTrue(getData(server.getRatedApplications(newExpertToken),
                RatedApplicationsResponse.class).applications.isEmpty());
    }
}
