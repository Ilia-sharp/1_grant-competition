package academy.tochkavhoda.competition;

import academy.tochkavhoda.competition.api.dto.requests.AddApplicationRequest;
import academy.tochkavhoda.competition.api.dto.requests.AddApplicationsRequest;
import academy.tochkavhoda.competition.api.dto.requests.ApplicationIdRequest;
import academy.tochkavhoda.competition.api.dto.responses.AddApplicationsResponse;
import academy.tochkavhoda.competition.api.dto.responses.ApplicationResponse;
import academy.tochkavhoda.competition.api.dto.responses.ApplicationsResponse;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestApplications extends TestBase {

    @Test
    public void testAddApplication() {
        String token = participant("romashka", "ООО Ромашка");
        int id1 = addApplication(token, "Умная теплица", 500_000, " Биология ", "ИНФОРМАТИКА");
        int id2 = addApplication(token, "Робот-пылесос", 300_000, "информатика");
        assertEquals(1, id1);
        assertEquals(2, id2);

        List<ApplicationResponse> applications =
                getData(server.getMyApplications(token), ApplicationsResponse.class).applications;
        assertEquals(2, applications.size());
        ApplicationResponse first = applications.get(0);
        assertEquals(id1, first.id);
        assertEquals("Умная теплица", first.title);
        assertEquals("Описание: Умная теплица", first.description);
        // направления приводятся к нижнему регистру и очищаются от пробелов
        assertEquals(Arrays.asList("биология", "информатика"), first.directions);
        assertEquals(500_000, first.requestedSum);
        assertEquals("ООО Ромашка", first.companyName);
        assertEquals(id2, applications.get(1).id);
    }

    @Test
    public void testAddSeveralApplications() {
        String token = participant("romashka", "ООО Ромашка");
        AddApplicationsRequest request = new AddApplicationsRequest(Arrays.asList(
                new AddApplicationRequest("Заявка 1", "Описание 1", Collections.singletonList("физика"), 100L),
                new AddApplicationRequest("Заявка 2", "Описание 2", Collections.singletonList("химия"), 200L),
                new AddApplicationRequest("Заявка 3", "Описание 3", Arrays.asList("физика", "химия"), 300L)));
        AddApplicationsResponse response =
                getData(server.addApplications(token, toJson(request)), AddApplicationsResponse.class);
        assertEquals(Arrays.asList(1, 2, 3), response.ids);
        assertEquals(Arrays.asList(1, 2, 3),
                ids(getData(server.getMyApplications(token), ApplicationsResponse.class)));
    }

    @Test
    public void testAddSeveralApplicationsIsAtomic() {
        String token = participant("romashka", "ООО Ромашка");
        AddApplicationsRequest request = new AddApplicationsRequest(Arrays.asList(
                new AddApplicationRequest("Заявка 1", "Описание 1", Collections.singletonList("физика"), 100L),
                new AddApplicationRequest("Заявка 2", "Описание 2", Collections.singletonList("химия"), -5L)));
        assertError(server.addApplications(token, toJson(request)),
                "Запрашиваемая сумма гранта должна быть положительной");
        assertTrue(getData(server.getMyApplications(token), ApplicationsResponse.class).applications.isEmpty());

        assertError(server.addApplications(token, toJson(new AddApplicationsRequest(Collections.emptyList()))),
                "Список заявок не может быть пустым");
        assertError(server.addApplications(token, "{}"), "Список заявок не может быть пустым");
        assertError(server.addApplications(token, "{\"applications\":[null]}"),
                "Заявка в списке не может быть пустой");
    }

    @Test
    public void testAddApplicationWrongData() {
        String token = participant("romashka", "ООО Ромашка");
        assertError(tryAddApplication(token, "  ", 100L, "физика"),
                "Название заявки должно быть непустым и не длиннее 200 символов");
        assertError(server.addApplication(token, toJson(new AddApplicationRequest("Заявка", null,
                        Collections.singletonList("физика"), 100L))),
                "Описание заявки не может быть пустым");
        assertError(tryAddApplication(token, "Заявка", 100L),
                "Список направлений не может быть пустым и не может содержать пустых направлений");
        assertError(tryAddApplication(token, "Заявка", 0L, "физика"),
                "Запрашиваемая сумма гранта должна быть положительной");
        assertError(tryAddApplication(token, "Заявка", null, "физика"),
                "Запрашиваемая сумма гранта должна быть положительной");
        assertError(server.addApplication(token,
                        "{\"title\":\"Заявка\",\"description\":\"Описание\",\"directions\":[\"физика\"],"
                                + "\"requestedSum\":\"много\"}"),
                "Некорректный JSON запроса");
    }

    @Test
    public void testOnlyParticipantCanAddApplication() {
        String expertToken = expert("expert1", "физика");
        assertError(tryAddApplication(expertToken, "Заявка", 100L, "физика"),
                "Операция доступна только участникам конкурса");
        assertError(tryAddApplication("bad-token", "Заявка", 100L, "физика"),
                "Неверный токен: пользователь не вошёл на сервер");
    }

    @Test
    public void testRemoveApplication() {
        String token = participant("romashka", "ООО Ромашка");
        int id1 = addApplication(token, "Заявка 1", 100, "физика");
        int id2 = addApplication(token, "Заявка 2", 200, "физика");
        assertSuccess(server.removeApplication(token, toJson(new ApplicationIdRequest(id1))));
        assertEquals(Collections.singletonList(id2),
                ids(getData(server.getMyApplications(token), ApplicationsResponse.class)));
        assertError(server.removeApplication(token, toJson(new ApplicationIdRequest(id1))),
                "Заявка с id " + id1 + " не найдена");
        assertError(server.removeApplication(token, "{}"), "Не указан id заявки");
    }

    @Test
    public void testCannotRemoveForeignApplication() {
        String owner = participant("romashka", "ООО Ромашка");
        String other = participant("vasilek", "ООО Василёк");
        int id = addApplication(owner, "Заявка", 100, "физика");
        assertError(server.removeApplication(other, toJson(new ApplicationIdRequest(id))),
                "Заявка с id " + id + " принадлежит другому участнику");
        assertEquals(1, getData(server.getMyApplications(owner), ApplicationsResponse.class).applications.size());
    }

    @Test
    public void testMyApplicationsContainOnlyOwnApplications() {
        String first = participant("romashka", "ООО Ромашка");
        String second = participant("vasilek", "ООО Василёк");
        int id1 = addApplication(first, "Заявка 1", 100, "физика");
        int id2 = addApplication(second, "Заявка 2", 100, "физика");
        int id3 = addApplication(first, "Заявка 3", 100, "физика");
        assertEquals(Arrays.asList(id1, id3),
                ids(getData(server.getMyApplications(first), ApplicationsResponse.class)));
        assertEquals(Collections.singletonList(id2),
                ids(getData(server.getMyApplications(second), ApplicationsResponse.class)));
    }
}
