package academy.tochkavhoda.competition;

import academy.tochkavhoda.competition.api.dto.requests.DirectionsRequest;
import academy.tochkavhoda.competition.api.dto.responses.ParticipantResponse;
import academy.tochkavhoda.competition.api.dto.responses.ParticipantsResponse;
import academy.tochkavhoda.competition.api.dto.responses.ServerResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestParticipants extends TestBase {

    private static List<String> companyNames(ParticipantsResponse response) {
        List<String> names = new ArrayList<>();
        for (ParticipantResponse participant : response.participants) {
            names.add(participant.companyName);
        }
        return names;
    }

    @Test
    public void testGetParticipants() {
        String first = participant("romashka", "Ромашка");
        String second = participant("vasilek", "Василёк");
        participant("astra", "Астра");
        addApplication(first, "Заявка 1", 100, "физика");
        addApplication(second, "Заявка 2", 100, "химия", "физика");
        String expertToken = expert("expert1", "физика");

        ServerResponse response = server.getParticipants(expertToken, null);
        ParticipantsResponse all = getData(response, ParticipantsResponse.class);
        // участники упорядочены по названию фирмы
        assertEquals(Arrays.asList("Астра", "Василёк", "Ромашка"), companyNames(all));
        assertEquals("Иван", all.participants.get(0).firstName);
        assertEquals("Иванов", all.participants.get(0).lastName);
        // логины и пароли участников наружу не отдаются
        assertFalse(response.getResponseData().contains(PASSWORD));
        assertFalse(response.getResponseData().contains("login"));

        ParticipantsResponse physics = getData(server.getParticipants(first,
                toJson(new DirectionsRequest(Collections.singletonList("физика")))), ParticipantsResponse.class);
        assertEquals(Arrays.asList("Василёк", "Ромашка"), companyNames(physics));

        ParticipantsResponse chemistry = getData(server.getParticipants(expertToken,
                toJson(new DirectionsRequest(Collections.singletonList("Химия")))), ParticipantsResponse.class);
        assertEquals(Collections.singletonList("Василёк"), companyNames(chemistry));
    }

    @Test
    public void testGetParticipantsWrongToken() {
        assertError(server.getParticipants("bad-token", null), "Неверный токен: пользователь не вошёл на сервер");
    }
}
