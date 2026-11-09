package academy.tochkavhoda.competition;

import academy.tochkavhoda.competition.api.dto.requests.DirectionsRequest;
import academy.tochkavhoda.competition.api.dto.responses.ApplicationsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestExpertApplications extends TestBase {
    private String expertToken;
    private String participantToken;
    private int math;
    private int itAndPhysics;
    private int mathAndIt;

    @BeforeEach
    public void prepare() {
        participantToken = participant("romashka", "ООО Ромашка");
        String other = participant("vasilek", "ООО Василёк");
        math = addApplication(participantToken, "Новый алгоритм", 100, "математика");
        itAndPhysics = addApplication(other, "Квантовый компьютер", 200, "информатика", "физика");
        addApplication(other, "Лазер", 300, "физика");
        addApplication(participantToken, "Новый сорт пшеницы", 400, "биология");
        mathAndIt = addApplication(other, "Криптография", 500, "математика", "информатика");
        expertToken = expert("expert1", "Математика", "информатика");
    }

    private List<Integer> byDirections(String... directions) {
        return ids(getData(server.getApplicationsByDirections(expertToken,
                toJson(new DirectionsRequest(Arrays.asList(directions)))), ApplicationsResponse.class));
    }

    @Test
    public void testApplicationsByOneDirection() {
        assertEquals(Arrays.asList(math, mathAndIt), byDirections("математика"));
        assertEquals(Arrays.asList(itAndPhysics, mathAndIt), byDirections("ИНФОРМАТИКА"));
    }

    @Test
    public void testApplicationsBySeveralDirections() {
        assertEquals(Arrays.asList(math, itAndPhysics, mathAndIt), byDirections("математика", "информатика"));
    }

    @Test
    public void testApplicationsByAllExpertDirections() {
        List<Integer> expected = Arrays.asList(math, itAndPhysics, mathAndIt);
        assertEquals(expected, ids(getData(server.getApplicationsByDirections(expertToken, null),
                ApplicationsResponse.class)));
        assertEquals(expected, ids(getData(server.getApplicationsByDirections(expertToken, "{}"),
                ApplicationsResponse.class)));
        assertEquals(expected, ids(getData(server.getApplicationsByDirections(expertToken,
                toJson(new DirectionsRequest(Collections.emptyList()))), ApplicationsResponse.class)));
    }

    @Test
    public void testApplicationsByForeignDirection() {
        assertError(server.getApplicationsByDirections(expertToken,
                        toJson(new DirectionsRequest(Arrays.asList("математика", "Физика")))),
                "Вы не являетесь экспертом по направлению «физика»");
    }

    @Test
    public void testOnlyExpertCanGetApplicationsByDirections() {
        assertError(server.getApplicationsByDirections(participantToken, null), "Операция доступна только экспертам");
    }

    @Test
    public void testRemovedApplicationDisappearsFromDirections() {
        assertSuccess(server.removeApplication(participantToken, "{\"applicationId\": " + math + "}"));
        assertEquals(Collections.singletonList(mathAndIt), byDirections("математика"));
    }
}
